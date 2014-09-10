package com.appsofawesome.keyboardutilities;


import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.findField;
import static de.robv.android.xposed.XposedHelpers.setObjectField;

import java.lang.reflect.Field;

import android.view.InputDevice;
import android.view.KeyEvent;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodHook.MethodHookParam;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;
//import android.view.WindowManagerPolicy; //for some reason, this is not publicly accessible


public class KeyConsumeHook implements IXposedHookLoadPackage{
	
	private boolean catchAltTab = true;
	private boolean catchWinTab = false;
	
	//private int btKeyboardID = 6;



	/* code for handling alt/meta tab from android source: https://github.com/android/platform_frameworks_base/blob/master/policy/src/com/android/internal/policy/impl/PhoneWindowManager.java
	 * 	
	 if (down && repeatCount == 0 && keyCode == KeyEvent.KEYCODE_TAB) {
            if (mRecentAppsDialogHeldModifiers == 0 && !keyguardOn) {
                final int shiftlessModifiers = event.getModifiers() & ~KeyEvent.META_SHIFT_MASK;
                if (KeyEvent.metaStateHasModifiers(shiftlessModifiers, KeyEvent.META_ALT_ON)
                        || KeyEvent.metaStateHasModifiers(
                                shiftlessModifiers, KeyEvent.META_META_ON)) {
                    mRecentAppsDialogHeldModifiers = shiftlessModifiers;
                    showOrHideRecentAppsDialog(RECENT_APPS_BEHAVIOR_EXIT_TOUCH_MODE_AND_SHOW);
                    return -1;
                }
            }
        } else if (!down && mRecentAppsDialogHeldModifiers != 0
                && (metaState & mRecentAppsDialogHeldModifiers) == 0) {
            mRecentAppsDialogHeldModifiers = 0;
            showOrHideRecentAppsDialog(keyguardOn ? RECENT_APPS_BEHAVIOR_DISMISS :
                    RECENT_APPS_BEHAVIOR_DISMISS_AND_SWITCH);
        }
	 */


	@Override
	public void handleLoadPackage(LoadPackageParam lpparam) throws Throwable {
		if (!lpparam.packageName.equals("android")) {
			//XposedBridge.log("We are in package: " + lpparam.packageName);
			return;
		}

		//get classes needed to hook method
		Class WindowManagerClass = findClass("com.android.internal.policy.impl.PhoneWindowManager", lpparam.classLoader);	
		Class WindowStateClass = findClass("android.view.WindowManagerPolicy$WindowState", lpparam.classLoader);

		//alternate method?
		//		Field theOneWeNeed = findField(WindowManagerClass, "mStatusBar");
		//		Class WindowStateClass = theOneWeNeed.getType();


		//actually hook the methods
		findAndHookMethod(WindowManagerClass, "interceptKeyBeforeDispatching", WindowStateClass, KeyEvent.class, int.class, hook1);
		
		//public int interceptKeyBeforeQueueing(KeyEvent event, int policyFlags, boolean isScreenOn) {
		//findAndHookMethod(WindowManagerClass, "interceptKeyBeforeQueueing", KeyEvent.class, int.class, boolean.class, hook2);
		
		//findAndHookMethod(WindowManagerClass, "preloadRecentApps", hook3);
		
		//public KeyEvent dispatchUnhandledKey(WindowState win, KeyEvent event, int policyFlags) {
		//findAndHookMethod(WindowManagerClass, "dispatchUnhandledKey", WindowStateClass, KeyEvent.class, int.class, hook4);
	}

	//the callback that overrides behavior in certain circumstances. 
	XC_MethodHook hook1 = new XC_MethodHook() {
		@Override
		protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
			
			//XposedBridge.log("Called Method with keyEvent " + param.args[1].toString());
			KeyEvent event = (KeyEvent) param.args[1];
			
			// code to stop android from eating alt-tab
			if (catchAltTab && event.getKeyCode() == KeyEvent.KEYCODE_TAB && event.isAltPressed()) {
				param.setResult(0); //tell it it was not handled and let the app handle it. 
			}
			
			if (catchWinTab && event.getKeyCode() == KeyEvent.KEYCODE_TAB && event.isMetaPressed()) {
				param.setResult(0); //tell it it was not handled and let the app handle it. 
			}
			
			//catch the power button that was passed along and change the KeyCode value
//			if (event.getKeyCode() == KeyEvent.KEYCODE_POWER && event.getDeviceId() == btKeyboardID) {
//				//TODO remove this when done. 
//				//I am trying to see if I can get better info on the keyboard and maybe identify exactly which kl file it uses
//				InputDevice device = event.getDevice();
//				XposedBridge.log("Device name: " + device.getName());
//				XposedBridge.log("Device descriptor: " + device.getDescriptor());
//				XposedBridge.log("Device vendor: " + device.getVendorId());
//				XposedBridge.log("Device product ID: " + device.getProductId());
//				XposedBridge.log("Device isVirtual: " + device.isVirtual());
//				XposedBridge.log("Device Type: " + device.getKeyboardType());
//				XposedBridge.log("Device Sources: " + device.getSources());
//				XposedBridge.log("Device kcm: " + device.getKeyCharacterMap());
//				param.setResult(0); //tell it it was not handled and let the app handle it. 
//			}
			
			
			
			
		}
		@Override
		protected void afterHookedMethod(MethodHookParam param) throws Throwable {

		}
	};
	
	
	XC_MethodHook hook2 = new XC_MethodHook() {
		
		@Override
		protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
			
			//XposedBridge.log("Called Method with keyEvent " + param.args[1].toString());
			KeyEvent event = (KeyEvent) param.args[0];
			int down = event.getAction();
			
			
			
			//code to remap sleep key on keyboard to fwd Del and vice versa
//			if (event.getKeyCode() == KeyEvent.KEYCODE_POWER ) {
//				if (!(Boolean) param.args[2]) { //screen is not on
//					return; //let it be handled normally as a wake up event
//				}
//				if (event.getDeviceId() == btKeyboardID) { //should not be hardcoded TODO figure out how to set this beforehand
////					String s = "dunno";
////					switch (down) {
////						case KeyEvent.ACTION_DOWN:
////							s = "down";
////							break;
////						case KeyEvent.ACTION_UP:
////							s = "up";
////							break;
////						case KeyEvent.ACTION_MULTIPLE:
////							s = "other";
////							break;
////					}
////					XposedBridge.log("caught power on keyboard device id " + btKeyboardID + " going " + s);
//					
//					//XposedBridge.log("caught power from BT keyboard");
//					param.setResult(1); //this stands for pass to user. should not be hard coded TODO
//				}
//				else {
//					//XposedBridge.log("power pressed on tablet");
//				}
//			}
//			
		}
		
		@Override
		protected void afterHookedMethod(MethodHookParam param) throws Throwable {
//			KeyEvent event = (KeyEvent) param.args[0];
//			String s = ((event.getKeyCode() == KeyEvent.KEYCODE_FORWARD_DEL? "CORRECT":"WRONG"));
//			XposedBridge.log("finished interceptKeyBeforeQueue with result " + param.getResult() + "and keycode is " + s);
		}
	};
	
	
	//This hooks the unhandled key dispatch method. Basically, this is where it checks for fallback actions, and the returned KeyEvent is the one it dispatches. so switches should happen here, I think. 
	XC_MethodHook hook4 = new XC_MethodHook() {
		@Override
		protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
			
//			KeyEvent event = (KeyEvent) param.args[1];
//			if (event.getKeyCode() == KeyEvent.KEYCODE_POWER && event.getDeviceId() == btKeyboardID) {
//				setObjectField(event, "mKeyCode", KeyEvent.KEYCODE_FORWARD_DEL);
//				param.setResult(event);
//				XposedBridge.log("changed keystroke before dispatch");
//				
//			}
		}
		
		@Override
		protected void afterHookedMethod(MethodHookParam param) throws Throwable {

		}
	};
	
	
	
	/* 
	The code that allows it to intercept shift space for language switching (from same link as above)
	
	// Handle keyboard language switching.
        if (down && repeatCount == 0
                && (keyCode == KeyEvent.KEYCODE_LANGUAGE_SWITCH
                        || (keyCode == KeyEvent.KEYCODE_SPACE
                                && (metaState & KeyEvent.META_CTRL_MASK) != 0))) {
            int direction = (metaState & KeyEvent.META_SHIFT_MASK) != 0 ? -1 : 1;
            mWindowManagerFuncs.switchKeyboardLayout(event.getDeviceId(), direction);
            return -1;
        }
        if (mLanguageSwitchKeyPressed && !down
                && (keyCode == KeyEvent.KEYCODE_LANGUAGE_SWITCH
                        || keyCode == KeyEvent.KEYCODE_SPACE)) {
            mLanguageSwitchKeyPressed = false;
            return -1;
        }
        
        Other relevant code bits:
        
        final boolean down = event.getAction() == KeyEvent.ACTION_DOWN;
        final int metaState = event.getMetaState();
        final int keyCode = event.getKeyCode();
        final int repeatCount = event.getRepeatCount();
	 
	 
	 */

	//The two main methods I am interested in: the first handles basic low level things like whether it should wake up the screen and even be processed beyond that
	//it handles media, volume, and power keys usually
	
	//the second handles anything that gets past the first to see if it is a special combination that should be consumed, such as alt tab, ctrl space, search + letter shortcut, etc
	
	//if it gets through both of those, 0 is returned and the app gets the key press.
	

	
	
	// public int interceptKeyBeforeQueueing(KeyEvent event, int policyFlags, boolean isScreenOn) {
	// from https://github.com/android/platform_frameworks_base/blob/kitkat-mr2.2-release/policy/src/com/android/internal/policy/impl/PhoneWindowManager.java#L3804
	
	// public long interceptKeyBeforeDispatching(WindowState win, KeyEvent event, int policyFlags)
	

}

/*
 * Notes: better way to consistently identify a device: 
 * http://grepcode.com/file/repository.grepcode.com/java/ext/com.google.android/android/4.4.4_r1/android/view/InputDevice.java#InputDevice.getDescriptor%28%29
 * 
 * this should work better than device ID, since that occasionally changes.
 * 
 */
