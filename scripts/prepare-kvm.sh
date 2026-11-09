#!/usr/bin/env bash
set -euo pipefail

mkdir -p .ci-diagnostics
exec > >(tee .ci-diagnostics/kvm-setup.txt) 2>&1

echo 'Inspecting runner and KVM device'
id
uname -a
if [[ ! -c /dev/kvm ]]; then
    echo '::error::This runner has no /dev/kvm character device. Hardware-accelerated emulator tests cannot start.'
    lscpu || true
    ls -ld /dev/kvm /sys/class/misc/kvm 2>/dev/null || true
    exit 1
fi
ls -l /dev/kvm

echo 'Installing KVM access rule on the disposable CI runner'
echo 'KERNEL=="kvm", GROUP="kvm", MODE="0666", OPTIONS+="static_node=kvm"' | sudo tee /etc/udev/rules.d/99-kvm4all.rules
sudo udevadm control --reload-rules
sudo timeout 30s udevadm trigger --settle --name-match=kvm
sudo udevadm settle --timeout=30

if [[ ! -r /dev/kvm || ! -w /dev/kvm ]]; then
    echo 'Applying the same read/write mode directly to the existing CI device'
    sudo chmod 0666 /dev/kvm
fi
ls -l /dev/kvm
if [[ ! -r /dev/kvm || ! -w /dev/kvm ]]; then
    echo '::error::The runner user cannot read and write /dev/kvm after permission setup.'
    exit 1
fi
if ! (exec 3<>/dev/kvm); then
    echo '::error::Opening /dev/kvm failed despite the visible permissions. Check runner virtualization support or device restrictions.'
    exit 1
fi
echo 'KVM device can be opened for read/write. Emulator startup will verify accelerator usability.'
