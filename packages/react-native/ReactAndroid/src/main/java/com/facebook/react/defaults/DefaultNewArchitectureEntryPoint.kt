/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

@file:Suppress("DEPRECATION") // We want to use ReactFeatureFlags here specifically

package com.facebook.react.defaults

import com.facebook.react.common.ReleaseLevel
import com.facebook.react.common.annotations.VisibleForTesting
import com.facebook.react.internal.featureflags.ReactNativeFeatureFlags
import com.facebook.react.internal.featureflags.ReactNativeFeatureFlagsOverrides_RNOSS_Canary_Android
import com.facebook.react.internal.featureflags.ReactNativeFeatureFlagsOverrides_RNOSS_Experimental_Android
import com.facebook.react.internal.featureflags.ReactNativeFeatureFlagsOverrides_RNOSS_Stable_Android
import com.facebook.react.internal.featureflags.ReactNativeFeatureFlagsProvider

/**
 * A utility class that serves as an entry point for users setup the New Architecture.
 *
 * This class needs to be invoked as `DefaultNewArchitectureEntryPoint.load(...)` by passing a
 * series of optional parameters.
 *
 * By default it loads a library called `appmodules`. `appmodules` is a convention used to refer to
 * the application dynamic library. If changed here should be updated also inside the template.
 *
 * By default it also enables both TurboModules, Fabric and Concurrent React (aka React 18), and
 * Bridgeless
 */
public object DefaultNewArchitectureEntryPoint {

  public var releaseLevel: ReleaseLevel = ReleaseLevel.STABLE

  /**
   * Loads the React Native New Architecture entry point with the default configuration.
   *
   * This will load the app with TurboModules, Fabric and Bridgeless by default.
   */
  @JvmStatic
  public fun load() {
    load(turboModulesEnabled = true, fabricEnabled = true, bridgelessEnabled = true)
  }

  @JvmStatic
  @Deprecated(
      message =
          "Loading the entry point with different flags for Fabric, TurboModule and Bridgeless is deprecated." +
              "Please use load() instead when loading the New Architecture.",
      replaceWith = ReplaceWith("load()"))
  public fun load(
      turboModulesEnabled: Boolean = true,
  ) {
    load(turboModulesEnabled, fabricEnabled = true, bridgelessEnabled = true)
  }

  @JvmStatic
  @Deprecated(
      message =
          "Loading the entry point with different flags for Fabric, TurboModule and Bridgeless is deprecated." +
              "Please use load() instead when loading the New Architecture.",
      replaceWith = ReplaceWith("load()"))
  public fun load(
      turboModulesEnabled: Boolean = true,
      fabricEnabled: Boolean = true,
  ) {
    load(turboModulesEnabled, fabricEnabled, bridgelessEnabled = true)
  }

  @JvmStatic
  @Deprecated(
      message =
          "Loading the entry point with different flags for Fabric, TurboModule and Bridgeless is deprecated." +
              "Please use load() instead when loading the New Architecture.",
      replaceWith = ReplaceWith("load()"))
  public fun load(
      turboModulesEnabled: Boolean = true,
      fabricEnabled: Boolean = true,
      bridgelessEnabled: Boolean = true
  ) {
    val (isValid, errorMessage) =
        isConfigurationValid(turboModulesEnabled, fabricEnabled, bridgelessEnabled)
    if (!isValid) {
      error(errorMessage)
    }

    when (releaseLevel) {
      ReleaseLevel.EXPERIMENTAL -> {
        ReactNativeFeatureFlags.override(
            ReactNativeFeatureFlagsOverrides_RNOSS_Experimental_Android())
      }
      ReleaseLevel.CANARY -> {
        ReactNativeFeatureFlags.override(ReactNativeFeatureFlagsOverrides_RNOSS_Canary_Android())
      }
      ReleaseLevel.STABLE -> {
        ReactNativeFeatureFlags.override(
            ReactNativeFeatureFlagsOverrides_RNOSS_Stable_Android(
                fabricEnabled, bridgelessEnabled, turboModulesEnabled))
      }
    }

    privateFabricEnabled = fabricEnabled
    privateTurboModulesEnabled = turboModulesEnabled
    privateConcurrentReactEnabled = fabricEnabled
    privateBridgelessEnabled = bridgelessEnabled

    DefaultSoLoader.maybeLoadSoLibrary()
  }

  @JvmStatic
  internal fun loadWithFeatureFlags(featureFlags: ReactNativeFeatureFlagsProvider) {
    ReactNativeFeatureFlags.override(featureFlags)

    privateFabricEnabled = featureFlags.enableFabricRenderer()
    privateTurboModulesEnabled = featureFlags.useTurboModules()
    privateConcurrentReactEnabled = featureFlags.enableFabricRenderer()
    privateBridgelessEnabled = featureFlags.enableBridgelessArchitecture()

    DefaultSoLoader.maybeLoadSoLibrary()
  }

  private var privateFabricEnabled: Boolean = false

  @JvmStatic
  public val fabricEnabled: Boolean
    get() = privateFabricEnabled

  private var privateTurboModulesEnabled: Boolean = false

  @JvmStatic
  public val turboModulesEnabled: Boolean
    get() = privateTurboModulesEnabled

  private var privateConcurrentReactEnabled: Boolean = false

  @JvmStatic
  public val concurrentReactEnabled: Boolean
    get() = privateConcurrentReactEnabled

  private var privateBridgelessEnabled: Boolean = false

  @JvmStatic
  public val bridgelessEnabled: Boolean
    get() = privateBridgelessEnabled

  @VisibleForTesting
  public fun isConfigurationValid(
      turboModulesEnabled: Boolean,
      fabricEnabled: Boolean,
      bridgelessEnabled: Boolean
  ): Pair<Boolean, String> =
      when {
        fabricEnabled && !turboModulesEnabled ->
            false to
                "fabricEnabled=true requires turboModulesEnabled=true (is now false) - Please update your DefaultNewArchitectureEntryPoint.load() parameters."
        bridgelessEnabled && (!turboModulesEnabled || !fabricEnabled) ->
            false to
                "bridgelessEnabled=true requires (turboModulesEnabled=true AND fabricEnabled=true) - Please update your DefaultNewArchitectureEntryPoint.load() parameters."
        else -> true to ""
      }
}
