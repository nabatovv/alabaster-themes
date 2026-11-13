# Alabaster Themes

<img src="../../alabaster-themes/src/main/resources/META-INF/pluginIcon.svg" style="max-width: 140px; width: 60px;"  alt="logo"/>

<!-- ![Build](https://github.com/nabatovv/alabaster-themes/workflows/Build/badge.svg) -->

[![Version](https://img.shields.io/jetbrains/plugin/v/20748-alabaster-themes.svg)](https://plugins.jetbrains.com/plugin/20748-alabaster-themes)
[![Downloads](https://img.shields.io/jetbrains/plugin/d/20748-alabaster-themes.svg)](https://plugins.jetbrains.com/plugin/20748-alabaster-themes)
<!-- ALL-CONTRIBUTORS-BADGE:START - Do not remove or modify this section -->
[![All Contributors](https://img.shields.io/badge/all_contributors-1-orange.svg)](#contributors-)
<!-- ALL-CONTRIBUTORS-BADGE:END -->

<!-- Plugin description -->
Tired of motley editor themes that have more colors than a chameleon? This theme is for you. 
<!-- Plugin description end -->

## Installation

- [](https://)Using IDE built-in plugin system:

  <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>Marketplace</kbd> > <kbd>Search for "alabaster-themes"</kbd> >
  <kbd>Install Plugin</kbd>
- Manually:

  Download the [latest release](https://github.com/nabatovv/alabaster-themes/releases) and install it manually using
  <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>⚙️</kbd> > <kbd>Install plugin from disk...</kbd>

<img src="images/collage-cropped.png" style="max-width: 2000px; width: 100%" alt="collage"/>

## Releasing

Releases are published to the JetBrains Marketplace from GitHub Actions.

1. Bump `pluginVersion` in `gradle.properties` and add the matching section to `CHANGELOG.md`.
2. Merge to `main` – the **Build** workflow builds and verifies the plugin.
3. Publish a GitHub release (e.g. tag `v1.0.96`) – the **Release** workflow builds the plugin, runs the Plugin Verifier and uploads it to the Marketplace.
   The workflow can also be started manually from the Actions tab.

Required repository secret (Settings → Secrets and variables → Actions):

| Secret           | Description                                                                 |
|------------------|-----------------------------------------------------------------------------|
| `PUBLISH_TOKEN`  | Permanent JetBrains Marketplace token (`JETBRAINS_MARKETPLACE_TOKEN` also accepted) |

Optional secrets used by `signPlugin`; when they are absent, the Marketplace signs the upload itself:

| Secret                  | Description                                  |
|-------------------------|----------------------------------------------|
| `CERTIFICATE_CHAIN`     | PEM-encoded certificate chain                |
| `PRIVATE_KEY`           | PEM-encoded private key                      |
| `PRIVATE_KEY_PASSWORD`  | Password of the private key                  |

The version taken from `gradle.properties` cannot be uploaded twice – if it is already on the Marketplace the workflow skips publishing and reports it, so bump `pluginVersion` to release again.

## Contributors

<!-- ALL-CONTRIBUTORS-LIST:START - Do not remove or modify this section -->

<!-- prettier-ignore-start -->
<!-- markdownlint-disable -->
<table>
  <tbody>
    <tr>
      <td align="center" valign="top" width="14.28%"><a href="https://bitbucket.org/Lopinopulos"><img src="https://avatars.githubusercontent.com/u/1469636?v=4?s=100" width="100px;" alt="Nikolai Lopin"/><br /><sub><b>Nikolai Lopin</b></sub></a><br /><a href="https://github.com/nabatovv/alabaster-themes/commits?author=nlopin" title="Code">💻</a></td>
    </tr>
  </tbody>
</table>

<!-- markdownlint-restore -->
<!-- prettier-ignore-end -->

<!-- ALL-CONTRIBUTORS-LIST:END -->
<!-- prettier-ignore-start -->

<!-- markdownlint-disable -->

<!-- markdownlint-restore -->

<!-- prettier-ignore-end -->

<!-- ALL-CONTRIBUTORS-LIST:END -->
