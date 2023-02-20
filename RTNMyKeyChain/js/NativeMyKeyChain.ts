import type {TurboModule} from 'react-native/Libraries/TurboModule/RCTExport';
import {TurboModuleRegistry} from 'react-native';

export interface Spec extends TurboModule {
  savePassword(password: string): Promise<{}>;
  getPassword(): Promise<{password: string}>;
  deletePassword(): Promise<{}>;
}

export default TurboModuleRegistry.get<Spec>('RTNMyKeyChain') as Spec | null;
