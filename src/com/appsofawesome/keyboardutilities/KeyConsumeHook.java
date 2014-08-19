package com.appsofawesome.keyboardutilities;


import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.findField;

import java.lang.reflect.Field;

import android.view.KeyEvent;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodHook.MethodHookParam;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;



public class KeyConsumeHook implements IXposedHookLoadPackage{
	
	private boolean catchAltTab = true;
	private boolean catchWinTab = false;



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
		findAndHookMethod(WindowManagerClass, "interceptKeyBeforeQueueing", KeyEvent.class, int.class, boolean.class, hook2);
	}

	//the callback that overrides behavior in certain circumstances. 
	XC_MethodHook hook1 = new XC_MethodHook() {
		@Override
		protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
			
			XposedBridge.log("Called Method with keyEvent " + param.args[1].toString());
			KeyEvent event = (KeyEvent) param.args[1];
			
			// code to stop android from eating alt-tab
			if (catchAltTab && event.getKeyCode() == KeyEvent.KEYCODE_TAB && event.isAltPressed()) {
				param.setResult(0); //tell it it was not handled and let the app handle it. 
			}
			
			if (catchWinTab && event.getKeyCode() == KeyEvent.KEYCODE_TAB && event.isMetaPressed()) {
				param.setResult(0); //tell it it was not handled and let the app handle it. 
			}
			
			//removed code trying to  stop the language switch from happening due to Shift space consumption. It worked, except that the consumption is handled at a higher level by the samsung keyboard app.
//			else if (event.getAction() == KeyEvent.ACTION_DOWN && event.getRepeatCount() == 0
//	                && (event.getKeyCode() == KeyEvent.KEYCODE_LANGUAGE_SWITCH
//                    || (event.getKeyCode() == KeyEvent.KEYCODE_SPACE
//                            && (event.getMetaState() & KeyEvent.META_SHIFT_MASK) != 0))) {
//				
//				/* if (event.getKeyCode() == KeyEvent.KEYCODE_SPACE && (event.isShiftPressed())) { */
//				XposedBridge.log("Shift + Space detected.");
//				param.setResult(0); //tell it it was not handled and let the app handle it. (just send shit+ space for the app to handle)
//				return;
//			}
//			
//			else if (event.getKeyCode() == KeyEvent.KEYCODE_LANGUAGE_SWITCH) {
//				XposedBridge.log("language switch detected.");
//				//leave this alone for now
//			}
			
//			param.setResult(0);
			
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
			if (event.getKeyCode() == KeyEvent.KEYCODE_POWER ) {
				if (event.getDeviceId() == 6) { //should not be hardcoded TODO figure out how to set this beforehand
					//XposedBridge.log("Called Method with keyEvent " + param.args[1].toString());
					String s = "dunno";
					switch (down) {
						case KeyEvent.ACTION_DOWN:
							s = "down";
							break;
						case KeyEvent.ACTION_UP:
							s = "up";
							break;
						case KeyEvent.ACTION_MULTIPLE:
							s = "repeat";
							break;
					}
					XposedBridge.log("caught power on keyboard device id 6 going " + s);
				}
				else {
					XposedBridge.log("power pressed on tablet");
				}
			}
			
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
