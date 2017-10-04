package com.casio.xshock;

import android.app.Application;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.XModuleResources;
import android.media.AudioManager;
import android.view.KeyEvent;

import de.robv.android.xposed.IXposedHookInitPackageResources;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_InitPackageResources;
import de.robv.android.xposed.callbacks.XC_LoadPackage;


public class xshock implements IXposedHookLoadPackage, IXposedHookInitPackageResources, IXposedHookZygoteInit {
    private String MODULE_PATH;

    private Context context;

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {
        if (!loadPackageParam.packageName.equals(hooks.gshockPKG)) {
            return;
        }

        XposedBridge.log("X-Shock Started");

        XposedHelpers.findAndHookMethod(Application.class, "attach", Context.class, new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(final MethodHookParam aparam) throws Throwable {
                        XposedBridge.log("X-Shock Activity");
                        context = (Context) aparam.args[0];
                    }
                });

        XposedHelpers.findAndHookMethod(hooks.gshockBaseActivity, loadPackageParam.classLoader, "showRequestLocationEnableDialog", new XC_MethodReplacement() {
            @Override
            protected Object replaceHookedMethod(final MethodHookParam aparam) throws Throwable {
                XposedBridge.log("X-Shock Location");
                return null;
            }
        });

        XposedHelpers.findAndHookMethod(Service.class, "onTaskRemoved", Intent.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(final MethodHookParam aparam) throws Throwable {
                XposedBridge.log("X-Shock Removed Task");
                ((Service)aparam.thisObject).stopForeground(true);
                super.afterHookedMethod(aparam);
            }
        });

        XposedHelpers.findAndHookMethod(hooks.gshockControl, loadPackageParam.classLoader, "changeVolume",
                "com.casio.gshockplus.ble.server.KeyCommanderService.VolumeEvent", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(final MethodHookParam aparam) throws Throwable {
                XposedBridge.log("X-Shock Volume Changed");
                AudioManager audio = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
                audio.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_SAME, AudioManager.FLAG_SHOW_UI);

                super.afterHookedMethod(aparam);
            }
        });

        XposedHelpers.findAndHookMethod(hooks.gshockControl, loadPackageParam.classLoader, "sendMusicIntent",
                int.class, int.class, new XC_MethodReplacement() {

                    @Override
                    protected  Object replaceHookedMethod (MethodHookParam aparam) throws Throwable {
                        if(((int)aparam.args[0]) != 0)
                        {
                            return null;
                        }

                        int key = (int) aparam.args[1];

                        if(key == 87)
                        {
                            XposedBridge.log("X-Shock Key Next");
                            //Next
                            Util.inputKeyevent(KeyEvent.KEYCODE_MEDIA_NEXT);
                        }
                        else if(key == 88)
                        {
                            XposedBridge.log("X-Shock Key Back");
                            //Back
                            Util.inputKeyevent(KeyEvent.KEYCODE_MEDIA_PREVIOUS);
                        }
                        else if(key == 85)
                        {
                            XposedBridge.log("X-Shock Key Play Pause");
                            //Play Pause
                            Util.inputKeyevent(KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE);
                        }
                        else
                        {
                            XposedBridge.log("X-Shock Key Unknown");
                        }

                        return null;
                    }
                });

        XposedBridge.log("X-Shock Hooked");
    }


    @Override
    public void handleInitPackageResources(XC_InitPackageResources.InitPackageResourcesParam resParam) throws Throwable {
        if (!resParam.packageName.equals(hooks.gshockPKG)) {
            return;
        }
        final XModuleResources modRes = XModuleResources.createInstance(MODULE_PATH, resParam.res);

    }

    @Override
    public void initZygote(StartupParam startupParam) throws Throwable {
        MODULE_PATH = startupParam.modulePath;
    }
}
