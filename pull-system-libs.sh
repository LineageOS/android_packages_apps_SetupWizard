#!/usr/bin/env bash
# SPDX-FileCopyrightText: The LineageOS Project
# SPDX-License-Identifier: Apache-2.0

set -euo pipefail

repo_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
default_out_dir="$(cd "$repo_dir/../../.." && pwd)/out"
out_dir="${1:-${OUT_DIR:-$default_out_dir}}"
system_libs_dir="$repo_dir/system_libs"

declare -A jars=(
  ["framework.jar"]="$out_dir/soong/.intermediates/frameworks/base/framework-minus-apex/android_common/combined/framework.jar"
  ["framework-location.jar"]="$out_dir/soong/.intermediates/frameworks/base/location/framework-location.impl/android_common/turbine/framework-location.jar"
  ["telephony-common.jar"]="$out_dir/soong/.intermediates/frameworks/opt/telephony/telephony-common/android_common/turbine-combined/telephony-common.jar"
  ["SettingsLib.jar"]="$out_dir/soong/.intermediates/frameworks/base/packages/SettingsLib/SettingsLib/android_common/turbine/SettingsLib.jar"
  ["org.lineageos.platform.internal.jar"]="$out_dir/soong/.intermediates/lineage-sdk/org.lineageos.platform.internal/android_common/turbine-combined/org.lineageos.platform.internal.jar"
  ["zxing-core.jar"]="$out_dir/soong/.intermediates/external/zxing/zxing-core/android_common/turbine/zxing-core.jar"
  ["setupcompat.jar"]="$out_dir/soong/.intermediates/external/setupcompat/setupcompat/android_common/turbine/setupcompat.jar"
  ["setupcompat-kt.jar"]="$out_dir/soong/.intermediates/external/setupcompat/setupcompat/android_common/kotlin_headers/setupcompat.jar"
  ["setupcompat-R.jar"]="$out_dir/soong/.intermediates/external/setupcompat/setupcompat/android_common/busybox/R.jar"
  ["PartnerConfig.jar"]="$out_dir/soong/.intermediates/external/setupcompat/PartnerConfig/android_common/turbine/PartnerConfig.jar"
  ["PartnerConfig-kt.jar"]="$out_dir/soong/.intermediates/external/setupcompat/PartnerConfig/android_common/kotlin_headers/PartnerConfig.jar"
  ["setupdesign.jar"]="$out_dir/soong/.intermediates/external/setupdesign/setupdesign/android_common/turbine/setupdesign.jar"
  ["setupdesign-kt.jar"]="$out_dir/soong/.intermediates/external/setupdesign/setupdesign/android_common/kotlin_headers/setupdesign.jar"
  ["setupdesign-R.jar"]="$out_dir/soong/.intermediates/external/setupdesign/setupdesign/android_common/busybox/R.jar"
)

missing=0
for name in "${!jars[@]}"; do
  if [[ ! -f "${jars[$name]}" ]]; then
    echo "Missing source jar for $name: ${jars[$name]}" >&2
    missing=1
  fi
done

if [[ "$missing" -ne 0 ]]; then
  echo "Build the required targets first, then rerun this script." >&2
  exit 1
fi

mkdir -p "$system_libs_dir"

for name in "${!jars[@]}"; do
  install -m 0644 "${jars[$name]}" "$system_libs_dir/$name"
done

echo "Populated $system_libs_dir with:"
for name in $(printf '%s\n' "${!jars[@]}" | sort); do
  echo "  $name"
done
