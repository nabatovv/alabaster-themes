#!/usr/bin/env bash
# Full shell syntax sample.
# Covers variables, arrays, arithmetic, conditionals, case, loops, functions,
# pipes, redirections, subshells, traps, heredocs and command substitution.

set -euo pipefail
IFS=$'\n\t'

readonly MAX_ITEMS=1024
GREETING="hello"
ENABLED=true
RATIO=1.5

declare -a numbers=(1 2 3 42)
declare -A settings=([host]="localhost" [port]="8080")

greet() {
    local name="${1:-world}"
    local punctuation="${2:-!}"
    printf '%s, %s%s\n' "$GREETING" "$name" "$punctuation"
}

sum() {
    local total=0
    local value
    for value in "$@"; do
        (( total += value ))
    done
    echo "$total"
}

classify() {
    case "$1" in
        0) echo "zero" ;;
        [1-9]) echo "single digit" ;;
        -*) echo "negative" ;;
        *) echo "large" ;;
    esac
}

main() {
    local count=0
    while (( count < 3 )); do
        (( count++ ))
    done

    until (( count == 0 )); do
        (( count-- ))
    done

    for index in "${!numbers[@]}"; do
        printf '%d:%d\n' "$index" "${numbers[$index]}"
    done

    for key in "${!settings[@]}"; do
        printf '%s=%s\n' "$key" "${settings[$key]}"
    done

    if [[ -n "${GREETING}" && "$ENABLED" == "true" ]]; then
        greet "shell" "!"
    elif [[ $RATIO =~ ^[0-9]+\.[0-9]+$ ]]; then
        echo "ratio: $RATIO"
    else
        echo "fallback"
    fi

    local total
    total=$(sum "${numbers[@]}")
    printf 'total=%s classify=%s\n' "$total" "$(classify "$total")"

    local temporary
    temporary=$(mktemp) || exit 1
    trap 'rm -f "$temporary"' EXIT INT TERM

    cat >"$temporary" <<EOF
# heredoc with expansion
greeting=${GREETING}
max=${MAX_ITEMS}
EOF

    cat <<'EOF'
# heredoc without expansion
$GREETING stays literal
EOF

    sort -u "$temporary" | while read -r line; do
        echo "> $line"
    done

    local result
    result=$(echo "pipe content" | tr 'a-z' 'A-Z' | rev)
    echo "$result"

    ( cd /tmp && pwd )
    echo "subshell returned: $?"

    printf 'line one\nline two\nline three\n' | grep -v 'two' && true

    local -i counter=0
    for (( counter = 0; counter < 2; counter++ )); do
        : # no-op
    done

    echo "exit status of false: $((0 + 1))"
}

main "$@"
