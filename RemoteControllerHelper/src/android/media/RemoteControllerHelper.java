package android.media;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;

/*
 * Provides access to fields and methods from outside the android.media package
 * Including:
 * 	RemoteController
 * 		-getArtworkSize()
 * 		-getRcDisplay()
 * 		-setIsRegistered(bool)
 * 		-getRemoteControlClientPackageName()
 * 
 * Also provides access alternative method for RemoteController registration.
 */

public class RemoteControllerHelper{
	private RemoteController mRemoteController;
	private Context mContext;
	private static IAudioService sService;
	private String TAG = "RemoteControllerHelper";
	
	
	public RemoteControllerHelper(RemoteController remoteController, Context context){
		mRemoteController = remoteController;
		mContext = context;
	}
	
	private static IAudioService getService() {
        if (sService != null) {
            return sService;
        }
        IBinder b = ServiceManager.getService(Context.AUDIO_SERVICE);
        sService = IAudioService.Stub.asInterface(b);
        return sService;
    }
	
	public Intent getCurrentClientIntent() throws NameNotFoundException {
		if (mRemoteController == null) return null;
		return mContext.getPackageManager().getLaunchIntentForPackage(mRemoteController.getRemoteControlClientPackageName());
		
	}
	
	public String getRemoteControlClientPackageName() {
		return mRemoteController.getRemoteControlClientPackageName();
	}
	
	public void setIsRegistered(boolean registered) {
		mRemoteController.setIsRegistered(registered);
	}
	
	public IRemoteControlDisplay getRcDisplay() {
		return mRemoteController.getRcDisplay();
	}
	
	public int[] getArtworkSize() {
		return mRemoteController.getArtworkSize();
	}
	
	public boolean registerRemoteController(RemoteController rctlr) {
        if (rctlr == null) {
            return false;
        }
        IAudioService service = getService();
        final RemoteController.OnClientUpdateListener l = rctlr.getUpdateListener();
        final ComponentName listenerComponent;
        
        //use actual class name if EnclosingClass is null
        if (l.getClass().getEnclosingClass() == null){
        	listenerComponent = new ComponentName(mContext, l.getClass());
        } else {
        	listenerComponent = new ComponentName(mContext, l.getClass().getEnclosingClass());
        }
        
        try {
            int[] artworkDimensions = rctlr.getArtworkSize();
            boolean reg = service.registerRemoteController(rctlr.getRcDisplay(),
                    artworkDimensions[0]/*w*/, artworkDimensions[1]/*h*/,
                    listenerComponent);
            rctlr.setIsRegistered(reg);
            return reg;
        } catch (RemoteException e) {
            Log.e(TAG, "Dead object in registerRemoteController " + e);
            return false;
        }
    }
}