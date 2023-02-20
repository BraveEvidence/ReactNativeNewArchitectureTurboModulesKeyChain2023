/**
 * Sample React Native App
 * https://github.com/facebook/react-native
 *
 * @format
 */

import React from 'react';
import type {PropsWithChildren} from 'react';
import {
  SafeAreaView,
  ScrollView,
  StatusBar,
  StyleSheet,
  Text,
  TouchableOpacity,
  useColorScheme,
  View,
} from 'react-native';

import {
  Colors,
  DebugInstructions,
  Header,
  LearnMoreLinks,
  ReloadInstructions,
} from 'react-native/Libraries/NewAppScreen';
import RTNMyKeyChain from 'rtn-my-key-chain/js/NativeMyKeyChain';

function App(): JSX.Element {
  const isDarkMode = useColorScheme() === 'dark';

  const backgroundStyle = {
    backgroundColor: isDarkMode ? Colors.darker : Colors.lighter,
  };

  const save = async () => {
    try {
      const data = await RTNMyKeyChain?.savePassword('mysupersecretpassword');
      console.log(data);
    } catch (e) {
      console.log(e);
    }
  };

  const get = async () => {
    try {
      const data = await RTNMyKeyChain?.getPassword();
      console.log(data);
    } catch (e) {
      console.log(e);
    }
  };

  const deletePassword = async () => {
    try {
      const data = await RTNMyKeyChain?.deletePassword();
      console.log(data);
    } catch (e) {
      console.log(e);
    }
  };

  return (
    <SafeAreaView style={backgroundStyle}>
      <StatusBar
        barStyle={isDarkMode ? 'light-content' : 'dark-content'}
        backgroundColor={backgroundStyle.backgroundColor}
      />
      <TouchableOpacity onPress={save}>
        <Text>Save Password</Text>
      </TouchableOpacity>
      <TouchableOpacity onPress={get}>
        <Text>Get Password</Text>
      </TouchableOpacity>
      <TouchableOpacity onPress={deletePassword}>
        <Text>Delete Password</Text>
      </TouchableOpacity>
    </SafeAreaView>
  );
}

// yarn add ./RTNMyKeyChain

// node rnapp/node_modules/react-native/scripts/generate-codegen-artifacts.js \
//   --path rnapp/ \
//   --outputPath rnapp/RTNMyKeyChain/generated/

export default App;
