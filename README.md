# Welcome to Access To!

[![](https://jitpack.io/v/paz-lavi/AccessTo.svg)](https://jitpack.io/#paz-lavi/AccessTo) [![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://github.com/paz-lavi/AccessTo/blob/master/LICENSE)
## Table of Contents
* [What Is It for](https://github.com/paz-lavi/AccessTo/blob/master/README.md#what-is-it-for)
* [Sample App](https://github.com/paz-lavi/AccessTo/blob/master/README.md#sample-app)
* [Intgration](https://github.com/paz-lavi/AccessTo/blob/master/README.md#integration)
* [How To Use](https://github.com/paz-lavi/AccessTo/blob/master/README.md#how-to-use)
* [API](https://github.com/paz-lavi/AccessTo/blob/master/README.md#api)
* [Callbacks](https://github.com/paz-lavi/AccessTo/blob/master/README.md#callbacks)
* [License](https://github.com/paz-lavi/AccessTo/blob/master/README.md#license) 


## What Is It for

An android package to handle permissions easily. With this package, you will never get stuck on "don't ask me again".
Ask permissions with dialog and transfer to the app setting to garnt permissions if needed.

## Sample App
Sample app can be found [here](https://github.com/paz-lavi/AccessToDemo)

## Integration

Add it in your root build.gradle at the end of repositories:
```css
	allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
```
Add the dependency

```css
	dependencies {
	        implementation 'com.github.paz-lavi:AccessTo:1.0.7'
	}
```
##  How To Use

**1.** Create an instance in your activity
```Java
    GiveMe giveMe = new GiveMe(this);
    giveMe.setGrantListener(new GrantListener() {  
    @Override  
    public void onGranted(boolean allGranted) {  
          
    }  
  
    @Override  
    public void onNotGranted(String[] permissions) {  
  
    }  
  
    @Override  
    public void onNeverAskAgain(String[] permissions) {  
  
    }  
});
```
or
```Java
GiveMe giveMe = new GiveMe(this, new GrantListener() {  
    @Override  
  public void onGranted(boolean allGranted) {  
          
    }  
  
  @Override  
  public void onNotGranted(String[] permissions) {  
  
    }  
  
  @Override  
  public void onNeverAskAgain(String[] permissions) {  
  
    }  
});
```

**2.** Add the next methods to your activity
```Java
@Override  
public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {  
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);  
    giveMe.onRequestPermissionsResult(requestCode, permissions, grantResults);  
}  
  
@Override  
protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {  
    super.onActivityResult(requestCode, resultCode, data);  
    giveMe.onActivityResult(requestCode, resultCode, data);  
}
```

**3.** Now you can use any of the API method

## API
### Note: when using method witout passing GrantListener the libary will use the last used GrantListener. 
* **Request Permissions:** request permissions from the user.
```Java
public void requestPermissions(@NonNull String[] permissions, @NonNull GrantListener grantListener) 	
```
```Java
public void requestPermissions(@NonNull String[] permissions) 
```

* **Ask Permissions From Setting:** request the user to grant permissions from the app setting with dialog first. 
```Java
public void askPermissionsFromSetting(String msg, String[] permissions, DialogListener dialogListener)	
```
```Java
public void askPermissionsFromSetting(String msg, String[] permissions, @NonNull GrantListener grantListener, DialogListener dialogListener) 
```

*  **Request Permissions With Force:** request permissions from the user. in the user select "don't ask me again" dialog will open.  if select "re-try" button the user will transfer to app setting to grant permissions
```Java
public void requestPermissionsWithForce(@NonNull String[] permissions, @NonNull GrantListener grantListener, String msg, DialogListener dialogListener) 
```
```Java
public void requestPermissionsWithForce(@NonNull String[] permissions, String msg, DialogListener dialogListener) 
```

* **Request Permissions With Dialog:** request permissions from the user with dialog first. If the user select "agree" the grant permissions dialogs will show or the user will transfer to app setting to grant permissions in case he select "don't ask me again"
```Java
public void requestPermissionsWithDialog(@NonNull String[] permissions, @NonNull GrantListener grantListener, String title, String msg, DialogListener dialogListener) 	
```
```Java
public void requestPermissionsWithDialog(@NonNull String[] permissions, String title, String msg, DialogListener dialogListener) 	
```

* **Set Grant Listener**
```Java
public void setGrantListener(GrantListener grantListener) 
```

## Callbacks
* **GrantListener**
```Java
void onGranted(boolean allGranted);  
  
void onNotGranted(String[] permissions);  
  
void onNeverAskAgain(String[] permissions); 
```
* **DialogListener**
```Java
void onPositiveButton();  
  
void onNegativeButton();
```
## License 

```
Copyright 2020 Paz Lavi

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

 https://github.com/paz-lavi/AccessTo/blob/master/LICENSE

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
