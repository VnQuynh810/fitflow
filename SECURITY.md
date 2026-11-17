# Security Policy

## Supported Versions

Only the latest `main` branch and latest release tag receive security updates.

| Version | Supported          |
| ------- | ------------------ |
| 2.0.x   | :white_check_mark: |
| < 2.0   | :x:                |

## API Keys & Credentials

- **Never commit API keys** (`GEMINI_API_KEY`, signing keystores, or service tokens) to source control.
- Local development keys belong exclusively in `local.properties` (which is git-ignored) or are entered by end users at runtime via **Bring Your Own Key (BYOK)** in Settings.

## Reporting a Vulnerability

Please **do not** open a public GitHub issue for security vulnerabilities.

Instead, report vulnerabilities privately via [GitHub Private Vulnerability Reporting](https://github.com/VnQuynh810/fitflow/security/advisories/new). Include:
1. A clear description of the issue and affected component.
2. Steps to reproduce or proof-of-concept.
3. Potential impact assessment.

We aim to acknowledge reports within 72 hours and coordinate a fix before public disclosure.
