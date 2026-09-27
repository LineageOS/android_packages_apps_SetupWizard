<!--
SPDX-FileCopyrightText: The LineageOS Project
SPDX-License-Identifier: Apache-2.0
-->

LineageOS SetupWizard
=====================

Build with Android Studio
-------------------------
SetupWizard needs access to the system API, therefore it can't be built only
using the public SDK. You first need to generate the libraries with all the
needed classes. To do this:

 - Place this directory in its usual location in the Android source tree
   (`packages/apps/SetupWizard`), since resources are also read from
   `external/setupcompat` and `external/setupdesign`
 - Build the platform artifacts that provide the non-public classes used by
   the app. At minimum, the jars copied by `pull-system-libs.sh` must exist
   under `out/soong/.intermediates/`
 - Run `./pull-system-libs.sh` from this directory. By default it reads from
   `../../../out` relative to this repository path and will populate
   `system_libs/` with the jars Gradle expects:
   - `framework.jar`
   - `framework-location.jar`
   - `org.lineageos.platform.internal.jar`
   - `PartnerConfig.jar`, `PartnerConfig-kt.jar`
   - `SettingsLib.jar`
   - `setupcompat.jar`, `setupcompat-kt.jar`, `setupcompat-R.jar`
   - `setupdesign.jar`, `setupdesign-kt.jar`, `setupdesign-R.jar`
   - `telephony-common.jar`
   - `zxing-core.jar`
 - If your build output lives somewhere else, pass it explicitly:
   `./pull-system-libs.sh /path/to/out`

You need to do the above once, unless Android Studio can't find some symbol.
In that case, rebuild the relevant platform targets and rerun
`./pull-system-libs.sh`.

Note that the platform libraries are compile-only, so the resulting APK is
meant for development in Android Studio; the installable APK is still built
with Soong.
