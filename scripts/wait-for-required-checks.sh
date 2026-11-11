#!/usr/bin/env bash
# Shared polling helper for the approved release workflow.
# Requires GH_TOKEN, GH_REPO and GITHUB_SHA-style commit arguments.

wait_for_checks() {
    local sha="$1"
    local names_json="$2"
    local deadline=$((SECONDS + 2700))

    while [ "$SECONDS" -lt "$deadline" ]; do
        gh api "repos/$GH_REPO/commits/$sha/check-runs?per_page=100" > checks.json

        failed=$(jq -r --arg sha "$sha" --argjson names "$names_json" '
            .check_runs
            | map(select(.head_sha == $sha and .app.slug == "github-actions" and (.name as $n | $names | index($n))))
            | group_by(.name)
            | map(max_by(.id))
            | map(select(.status == "completed" and .conclusion != "success"))
            | map(.name)
            | join(", ")
        ' checks.json)
        if [ -n "$failed" ]; then
            echo "Required check failed for $sha: $failed" >&2
            exit 1
        fi

        if jq -e --arg sha "$sha" --argjson names "$names_json" '
            .check_runs
            | map(select(.head_sha == $sha and .app.slug == "github-actions"))
            | group_by(.name)
            | map(max_by(.id))
            | . as $runs
            | all($names[];
                . as $name
                | any($runs[];
                    .name == $name and .status == "completed" and .conclusion == "success"))
        ' checks.json >/dev/null; then
            echo "All required checks succeeded for $sha."
            return 0
        fi

        sleep 20
    done

    echo "Timed out waiting for required checks for $sha." >&2
    gh api "repos/$GH_REPO/commits/$sha/check-runs?per_page=100" > checks.json || true
    jq -r '.check_runs | group_by(.name) | map(max_by(.id)) | .[] | "\(.name): \(.status) \(.conclusion)"' checks.json >&2 || true
    exit 1
}
