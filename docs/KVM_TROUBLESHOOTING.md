# Emulator CI: KVM preparation failure

Date: 2026-11-09
Branch: update

## Reported failure

The supplied Actions log ends with exit code 1 in the old Enable KVM permissions step,
after printing the udev rule. This occurs before the emulator/test step. It is not evidence
that the corrected test-host assertion or a Compose test failed in this execution.

The original commands did not identify the failing command or print device permissions.
The final test -r /dev/kvm && test -w /dev/kvm may have run before asynchronous udev
processing completed. Missing device support or another command failure is also possible.
The exact cause is not established by the supplied excerpt.

## Change

The workflow calls scripts/prepare-kvm.sh. It records runner identity, kernel and device
permissions; rejects a missing/non-character /dev/kvm; installs the same udev rule; waits
for triggered events with a bounded timeout; and checks resulting read/write permissions.
If the device still lacks access, it applies the same 0666 mode directly to that existing
device. This permission change is limited to the disposable CI runner and matches the
previous rule's intended access policy. Do not reuse it on a shared production host.

The script finally tries to open the device for read/write without sudo. That checks more
than filesystem mode bits, but does not prove emulator acceleration works: actual startup
must still succeed. Missing virtualization support is not fixed by creating a fake device,
skipping checks or switching off tests. No runner version or test selection is changed.

Diagnostics are saved as .ci-diagnostics/kvm-setup.txt and included in the existing artifact
upload. udev command failures retain their nonzero outcome. A missing device or denied open
produces an explicit Actions error and a failing job.

## Verification

Source-reviewed only. The approved push can trigger both existing workflows; no successful
rerun is claimed. If KVM still fails, inspect kvm-setup.txt and the failing command before
changing runner/platform settings. A successful KVM step must then be followed by successful
emulator startup and all core instrumentation tests.
