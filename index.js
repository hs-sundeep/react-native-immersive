import { NativeModules, DeviceEventEmitter, Platform } from 'react-native'
const { RNFullscreen } = NativeModules

const warnInIOS = () => {
  if(__DEV__){
    console.warn(
      'Fullscreen module is not supported in IOS.' +
      'This message will disappear in a release build.'
    );
  }
}

let isListenerEnabled = false

const Fullscreen = Platform.OS === 'android' ? {
  on: () => RNFullscreen.setFullscreen(true),
  off: () => RNFullscreen.setFullscreen(false),
  setFullscreen: (isOn) => RNFullscreen.setFullscreen(isOn),
  getFullscreen: () => RNFullscreen.getFullscreen(), // do not always match actual display state
  addFullscreenListener: (listener) => {
    DeviceEventEmitter.addListener('@@FULLSCREEN_STATE_CHANGED', listener)
    if (isListenerEnabled) return
    isListenerEnabled = true
    RNFullscreen.addFullscreenListener()
  },
  removeFullscreenListener: (listener) => {
    DeviceEventEmitter.removeListener('@@FULLSCREEN_STATE_CHANGED', listener)
  }
} : {
  on: warnInIOS,
  off: warnInIOS,
  setFullscreen: warnInIOS,
  getFullscreen: warnInIOS,
  addFullscreenListener: warnInIOS,
  removeFullscreenListener: warnInIOS
}

export { Fullscreen }
export default Fullscreen
