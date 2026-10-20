#!/usr/bin/env bash
set -e

cd "$(dirname "$0")/.."

echo "=========================================================="
echo "⚡ Supabase Online CLI Helper"
echo "=========================================================="

if ! command -v supabase > /dev/null 2>&1; then
    echo "❌ Supabase CLI is not installed or not in PATH."
    exit 1
fi

echo "Supabase CLI version: $(supabase --version)"
echo ""
echo "Select an action:"
echo "1) supabase login              - Authenticate CLI with your Supabase account"
echo "2) supabase link               - Link local project to your online Supabase project"
echo "3) supabase db pull            - Pull latest online schema changes into migrations"
echo "4) supabase db push            - Push local migrations/schema to online Supabase"
echo "5) supabase projects list      - List your remote projects"
echo "6) Custom supabase command"
echo "7) Exit"
echo ""

read -rp "Enter choice [1-7]: " choice

case "$choice" in
    1)
        supabase login
        ;;
    2)
        read -rp "Enter Supabase Project Ref ID (e.g. abcdefghijklmno): " proj_id
        if [ -n "$proj_id" ]; then
            supabase link --project-ref "$proj_id"
        else
            echo "Project Ref ID cannot be empty."
        fi
        ;;
    3)
        supabase db pull
        ;;
    4)
        supabase db push
        ;;
    5)
        supabase projects list
        ;;
    6)
        read -rp "Enter custom supabase subcommand (e.g. migration new add_table): " custom_cmd
        # shellcheck disable=SC2086
        supabase $custom_cmd
        ;;
    *)
        echo "Exiting."
        exit 0
        ;;
esac
