package com.appsofawesome.alttabkillerpackage;

import static android.view.WindowManager.LayoutParams.*;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.findField;

import java.lang.reflect.Field;

import android.view.KeyEvent;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodHook.MethodHookParam;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;



public class AltTabHook implements IXposedHookLoadPackage{
	
	XC_MethodHook hook = new XC_MethodHook() {
        @Override
        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
        	XposedBridge.log("Called Method with keyEvent " + param.args[1].toString());
        	KeyEvent event = (KeyEvent) param.args[1];
        	
        	if (event.getKeyCode() == KeyEvent.KEYCODE_TAB && (event.isAltPressed() || event.isMetaPressed())) {
        		param.setResult(0); //tell it it was not handled and let the app handle it. 
        	}
        	
        }
        @Override
        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
           
        }
	};
	
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
		if (lpparam.packageName.equals("android")) {
			XposedBridge.log("We are in package: " + lpparam.packageName);
		}
		else {
			//XposedBridge.log("instead we are in " + lpparam.packageName);
			return;
		}
		//get classes needed to hook method
		
		Class WindowManagerClass = findClass("com.android.internal.policy.impl.PhoneWindowManager", lpparam.classLoader);
				//lpparam.classLoader.loadClass("com.android.server.wm.WindowsState");
		//Field WindowStateField = WindowManagerClass.getField("mStatusBar");
		//Class WindowStateClass = WindowStateField.getType();
		
//		Field[] fields = WindowManagerClass.getDeclaredFields();
//		for (Field f: fields) {
//			if (f.getType().getName().equals("android.view.WindowManagerPolicy$WindowState")) {
//				XposedBridge.log(f.getName());
//				XposedBridge.log("name: " + lpparam.appInfo.name);
//				XposedBridge.log("processName: " + lpparam.appInfo.processName);
//				
//				XposedBridge.log("end of package: " + lpparam.packageName);
//				XposedBridge.log("");
//				XposedBridge.log("");
//			}
//		}
		
		
		Field theOneWeNeed = findField(WindowManagerClass, "mStatusBar");
		Class WindowStateClass = theOneWeNeed.getType();
		
//		XposedBridge.log("name: " + lpparam.appInfo.name);
//		XposedBridge.log("processName: " + lpparam.appInfo.processName);
//		//lpparam.classLoader.getParent().
//		XposedBridge.log("end of package: " + lpparam.packageName);
//		XposedBridge.log("");
		//XposedBridge.log("");
		
		findAndHookMethod(WindowManagerClass, "interceptKeyBeforeDispatching", WindowStateClass, KeyEvent.class, int.class, hook);
	}


	
	
	// public long interceptKeyBeforeDispatching(WindowState win, KeyEvent event, int policyFlags)

}
