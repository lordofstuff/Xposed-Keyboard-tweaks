package com.appsofawesome.keyboardutilities;


import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;
import android.view.KeyEvent;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;


/**
 * Hooks into the method where it detects alt tab presses and stops android from eating them. 
 * @author Lordofstuff
 *
 */
public class KeyConsumeHook implements IXposedHookLoadPackage{
	
	private boolean catchAltTab = true;
	private boolean catchWinTab = false;
	


	@Override
	public void handleLoadPackage(LoadPackageParam lpparam) throws Throwable {
		if (!lpparam.packageName.equals("android")) {
			//XposedBridge.log("We are in package: " + lpparam.packageName);
			return;
		}

		//get classes needed to hook method
		Class WindowManagerClass = findClass("com.android.internal.policy.impl.PhoneWindowManager", lpparam.classLoader);	
		Class WindowStateClass = findClass("android.view.WindowManagerPolicy$WindowState", lpparam.classLoader);

		//actually hook the method
		findAndHookMethod(WindowManagerClass, "interceptKeyBeforeDispatching", WindowStateClass, KeyEvent.class, int.class, hook1);
	}

	//the callback that overrides behavior in certain circumstances. 
	XC_MethodHook hook1 = new XC_MethodHook() {
		@Override
		protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
			
			KeyEvent event = (KeyEvent) param.args[1];
			
			// code to stop android from eating alt-tab
			if (catchAltTab && event.getKeyCode() == KeyEvent.KEYCODE_TAB && event.isAltPressed()) {
				param.setResult(0); //tell it it was not handled and let the app handle it. 
			}
			
			if (catchWinTab && event.getKeyCode() == KeyEvent.KEYCODE_TAB && event.isMetaPressed()) {
				param.setResult(0); //tell it it was not handled and let the app handle it. 
			}		
		}
		
		@Override
		protected void afterHookedMethod(MethodHookParam param) throws Throwable {

		}
	};
}
	
	
	
		
		
		
	
	
	
	
	
	

	