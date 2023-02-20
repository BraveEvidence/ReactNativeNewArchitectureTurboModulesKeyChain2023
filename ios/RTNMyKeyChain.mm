//
//  RTNMyKeyChain.m
//  rnapp
//
//  Created by  on 20/02/23.
//

#import <Foundation/Foundation.h>
//#import "RTNCalculatorSpec.h"
#import "RTNMyKeyChain.h"
#import "rnapp-Swift.h"

@implementation RTNMyKeyChain
RCT_EXPORT_MODULE()

MyKeyChain *myKeyChain = [[MyKeyChain alloc] init];

//- (void)add:(double)a b:(double)b resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject {
//    NSNumber *result = [[NSNumber alloc] initWithInteger:a+b];
//    resolve(result);
//}


- (void)savePassword:(NSString *)password resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject{
  
  NSString *value =[myKeyChain savePasswordWithPassword:password];
    if ([value isEqual: @"Success"]){
      NSDictionary *dictRes = @{
        @"success": value,
      };
      resolve(dictRes);
    } else {
      reject(@"Fail",value,nil);
    }
}

- (void)getPassword:(RCTPromiseResolveBlock)resolve
             reject:(RCTPromiseRejectBlock)reject{
  
  NSString *value = [myKeyChain getPassword];
    if ([value isEqual: @"Failed to read password"]){
      reject(@"Fail",@"Failed to read password",nil);
    } else {
      NSDictionary *dictRes = @{
        @"password": value,
      };
      resolve(dictRes);
    }
}

- (void)deletePassword:(RCTPromiseResolveBlock)resolve
                reject:(RCTPromiseRejectBlock)reject{
  NSString *value = [myKeyChain deletePassword];
  if ([value isEqual: @"Success"]){
    NSDictionary *dictRes = @{
      @"success": value,
    };
    resolve(dictRes);
  } else {
    reject(@"Fail",value,nil);
  }
}



- (std::shared_ptr<facebook::react::TurboModule>)getTurboModule:
    (const facebook::react::ObjCTurboModule::InitParams &)params
{
    return std::make_shared<facebook::react::NativeMyKeyChainSpecJSI>(params);
}

@end
