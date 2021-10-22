/**
 * Sample React Native App
 * https://github.com/facebook/react-native
 *
 * @format
 * @flow strict-local
 */

import React, { useEffect, useState } from 'react';
import type { Node } from 'react';
import {
  Button,
  SafeAreaView,
  ScrollView,
  StatusBar,
  StyleSheet,
  Text,
  useColorScheme,
  View,
  DeviceEventEmitter,
  requireNativeComponent,
  // NativeModules
} from 'react-native';

// NativeModules.AliRtcZijinModule.joinChannel()

const AliRtcZijinView = requireNativeComponent('AliRtcZijinView')
console.log(AliRtcZijinView)

import {
  Colors,
  DebugInstructions,
  Header,
  LearnMoreLinks,
  ReloadInstructions,
} from 'react-native/Libraries/NewAppScreen';

const App: () => Node = () => {
  const isDarkMode = useColorScheme() === 'dark';

  const backgroundStyle = {
    backgroundColor: isDarkMode ? Colors.darker : Colors.lighter,
  };

  const [localStream, setLocalStream] = useState('123');

  useEffect(() => {
    DeviceEventEmitter.addListener('KeyEvent', (args) => {
      console.log(args)
    })


  }, [])

  return (
    <SafeAreaView style={backgroundStyle}>
      <StatusBar barStyle={isDarkMode ? 'light-content' : 'dark-content'} />
      <ScrollView
        contentInsetAdjustmentBehavior="automatic"
        style={backgroundStyle}>
        <View
          style={{
            backgroundColor: isDarkMode ? Colors.black : Colors.white,
          }}>
          {/* <AliRtcZijinView
            style={
              styles.localVideo
            }
            channel={"zijinRtc"}
          >
          </AliRtcZijinView> */}
        </View>
      </ScrollView>
    </SafeAreaView >
  );
};



const styles = StyleSheet.create({
  sectionContainer: {
    marginTop: 32,
    paddingHorizontal: 24,
  },
  sectionTitle: {
    fontSize: 24,
    fontWeight: '600',
  },
  sectionDescription: {
    marginTop: 8,
    fontSize: 18,
    fontWeight: '400',
  },
  highlight: {
    fontWeight: '700',
  },
  root: {
    backgroundColor: '#fff',
    flex: 1,
    padding: 20,
    justifyContent: 'center'
  },
  inputField: {
    marginBottom: 10,
    flexDirection: 'column',
  },
  videoContainer: {
    flex: 1,
  },
  videos: {
    width: '100%',
    flex: 1,
    position: 'relative',
    overflow: 'hidden',
    borderRadius: 6,
  },
  localVideos: {
    height: 0.5,
    marginBottom: 10,
  },
  remoteVideos: {
    height: 0.5,
  },
  localVideo: {
    height: 456,
    width: 320,
    // backgroundColor: 'red',
  },
  remoteVideo: {
    height: 1,
    width: 1,
  },
});

export default App;
