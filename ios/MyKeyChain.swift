//
//  MyKeyChain.swift
//  rnapp
//
//  Created by  on 20/02/23.
//

import Foundation

@objcMembers class MyKeyChain: NSObject {
  let yourEdgeService = "rnapp.com"
  let accountName = "rnapp"
  
  func savePassword(password: String) -> String{
    do {
      try save(service: yourEdgeService, account: accountName, password: password.data(using: .utf8) ?? Data())
      return "Success"
    } catch {
      return error.localizedDescription
    }
  }
  
  func getPassword() -> String? {
    guard let data = getData(service: yourEdgeService, account: accountName)
    else {
      return "Failed to read password"
    }
    
    let password = String(decoding: data,as: UTF8.self)
    return password
  }
  
  func deletePassword() -> String {
      let secItemClasses = [
        kSecClassGenericPassword,
        kSecClassInternetPassword,
        kSecClassCertificate,
        kSecClassKey,
        kSecClassIdentity
      ]
      for itemClass in secItemClasses {
          let spec: NSDictionary = [kSecClass: itemClass]
          SecItemDelete(spec)
      }
      return "Success"
    }
  
  enum KeychainError: Error {
    case duplicateEntry
    case unknown(OSStatus)
  }
  
  func save(
    service: String,account: String,password: Data
  ) throws {
    let query: [String: AnyObject] = [
      kSecClass as String: kSecClassGenericPassword,
      kSecAttrService as String: service as AnyObject,
      kSecAttrAccount as String: account as AnyObject,
      kSecValueData as String: password as AnyObject
    ]
    
    let status = SecItemAdd(query as CFDictionary, nil)
    
    guard status != errSecDuplicateItem else {
      throw KeychainError.duplicateEntry
    }
    
    guard status == errSecSuccess else {
      throw KeychainError.unknown(status)
    }
  }
  
  func getData(service: String,account: String) -> Data? {
    let query: [String: AnyObject] = [
      kSecClass as String: kSecClassGenericPassword,
      kSecAttrService as String: service as AnyObject,
      kSecAttrAccount as String: account as AnyObject,
      kSecReturnData as String: kCFBooleanTrue,
      kSecMatchLimit as String: kSecMatchLimitOne
    ]
    
    var result: AnyObject?
    SecItemCopyMatching(query as CFDictionary, &result)
    
    return result as? Data
  }
  
}




























































