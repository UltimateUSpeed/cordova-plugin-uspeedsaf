package uspeed;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.FileUtils;
import android.os.ParcelFileDescriptor;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Base64;
import android.util.Log;
import android.webkit.MimeTypeMap;
import android.webkit.ValueCallback;

import androidx.annotation.RequiresApi;
import androidx.documentfile.provider.DocumentFile;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.channels.FileChannel;
import java.util.HashMap;

public class USpeedSaf extends CordovaPlugin implements ValueCallback<String> {
	protected CallbackContext callbackContext;
	protected HashMap<String, String> saveFileData = new HashMap<>();
	protected CordovaInterface cordovaInterface;
	protected CordovaWebView cordovaWebView;

	public enum Action {
		selectFolder,
		selectFile,
		openFolder,
		openFile,
		readFile,
		writeFile,
		saveFile,
		existsFile,
	}

	@Override
	public void initialize(CordovaInterface cordovaInterface, CordovaWebView cordovaWebView) {
		this.cordovaInterface = cordovaInterface;
		this.cordovaWebView = cordovaWebView;
	}

	@Override
	public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
		Method method = null;
		try {
			method = this.getClass().getMethod(action, JSONArray.class, CallbackContext.class);
		} catch (Exception e) {
			debugLog(e);
		}
		if (method == null || !Modifier.isPublic(method.getModifiers()))
			return false;
		try {
			return (Boolean) method.invoke(this, args, callbackContext);
		} catch (Exception e) {
			callbackContext.error(debugLog(e));
			return false;
		}
	}

	public boolean selectFolder(JSONArray args, CallbackContext callbackContext) throws JSONException {
		Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
		String initialFolder = null;
		try {
			if (!args.isNull(0))
				initialFolder = args.getString(0);
		} catch (JSONException e) {
			debugLog(e);
		}
		if (initialFolder != null)
			intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, initialFolder);
		this.callbackContext = callbackContext;
		cordovaInterface.startActivityForResult(this, intent, Action.selectFolder.ordinal());
		return true;
	}

	public boolean selectFile(JSONArray args, CallbackContext callbackContext) throws JSONException {
		Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
		String initialFolder = null;
		try {
			if (!args.isNull(0))
				initialFolder = args.getString(0);
		} catch (JSONException e) {
			debugLog(e);
		}
		if (initialFolder != null)
			intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, initialFolder);
		intent.addCategory(Intent.CATEGORY_OPENABLE);
		intent.setType("*/*");
		intent.putExtra(Intent.EXTRA_MIME_TYPES, new String[] { "*/*" });
		intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
		this.callbackContext = callbackContext;
		cordovaInterface.startActivityForResult(this, Intent.createChooser(intent, "Select File"),
				Action.selectFile.ordinal());
		return true;
	}

	public boolean openFolder(JSONArray args, CallbackContext callbackContext) {
		try {
			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.setDataAndType(Uri.parse(args.getString(0)), DocumentsContract.Document.MIME_TYPE_DIR);
			this.callbackContext = callbackContext;
			cordovaInterface.startActivityForResult(this, Intent.createChooser(intent, "Open folder"),
					Action.openFolder.ordinal());
			return true;
		} catch (Exception e) {
			callbackContext.error(debugLog(e));
			return false;
		}
	}

	public boolean openFile(JSONArray args, CallbackContext callbackContext) {
		try {
			String uri = args.getString(0);
			Intent intent = new Intent(Intent.ACTION_VIEW);
			// String[] resourcesUri = this.getRealPathFromURI(Uri.parse(uri));
			// intent.setDataAndType(Uri.parse(uri), MimeTypeMap.getSingleton().getMimeTypeFromExtension(resourcesUri[0].substring(resourcesUri[0].lastIndexOf('.') + 1)));
			String[] info = this.getInfoFromUri(Uri.parse(uri));
			intent.setDataAndType(Uri.parse(uri), info[0]);

			intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			this.callbackContext = callbackContext;
			// cordova.getActivity().startActivity(Intent.createChooser(intent, "Open File
			// in..."));
			cordovaInterface.startActivityForResult(this, Intent.createChooser(intent, "Open File in..."),
					Action.openFile.ordinal());
			return true;
		} catch (Exception e) {
			callbackContext.error(debugLog(e));
			return false;
		}
	}

	@RequiresApi(api = Build.VERSION_CODES.Q)
	public boolean readFile(JSONArray args, CallbackContext callbackContext) {
		try (
				InputStream inputStream = cordovaInterface.getContext().getContentResolver()
						.openInputStream(Uri.parse(args.getString(0)));
				ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
			FileUtils.copy(inputStream, byteArrayOutputStream);
			callbackContext.success(byteArrayOutputStream.toByteArray());
			return true;
		} catch (Exception e) {
			callbackContext.error(debugLog(e));
			return false;
		}
	}

	public boolean writeFile(JSONArray args, CallbackContext callbackContext) {
		try {
			JSONObject params = args.getJSONObject(0);
			String filename = params.getString("filename");
			String mimeType = MimeTypeMap.getSingleton()
					.getMimeTypeFromExtension(filename.substring(filename.lastIndexOf('.') + 1));
			String folder = null;
			try {
				if (!params.isNull("folder"))
					folder = params.getString("folder");
			} catch (Exception e) {
				debugLog(e);
			}
			String subFolder = "";
			try {
				if (!params.isNull("subFolder"))
					subFolder = params.getString("subFolder");
			} catch (Exception e) {
				debugLog(e);
			}
			Uri uri = null;
			if (folder != null && !folder.trim().equals("")) {
				DocumentFile documentFile = DocumentFile.fromTreeUri(
						cordovaInterface.getContext(),
						Uri.parse(folder));
				if (subFolder != null) {
					String subFolders[] = subFolder.split("/");
					for (int i = 0; i < subFolders.length; ++i) {
						DocumentFile subFolderDocumentFile = null;
						for (DocumentFile subFile : documentFile.listFiles()) {
							if (subFile.isDirectory() && subFile.getName().equals(subFolder)) {
								subFolderDocumentFile = subFile;
								break;
							}
						}
						documentFile = subFolderDocumentFile != null ? subFolderDocumentFile
								: documentFile.createDirectory(subFolders[i]);
					}
				}
				DocumentFile file = null;
				for (DocumentFile subFile : documentFile.listFiles()) {
					if (!subFile.isDirectory() && subFile.getName().equals(filename)) {
						file = subFile;
						break;
					}
				}
				uri = (file != null ? file
						: documentFile.createFile(
								mimeType,
								filename))
						.getUri();
			} else {
				ContentResolver contentResolver = cordovaInterface.getContext().getContentResolver();
				ContentValues contentValues = new ContentValues();
				contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, filename);
				contentValues.put(MediaStore.MediaColumns.MIME_TYPE, mimeType);
				if (!subFolder.startsWith("/"))
					subFolder = "/" + subFolder;
				contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + subFolder);
				uri = contentResolver.insert(MediaStore.Files.getContentUri("external"), contentValues);
			}
			try (OutputStream outputStream = cordovaInterface.getContext().getContentResolver().openOutputStream(uri)) {
				outputStream.write(Base64.decode(params.getString("data"), Base64.DEFAULT));
			}
			callbackContext.success(uri.toString());
			return true;
		} catch (Exception e) {
			callbackContext.error(debugLog(e));
			return false;
		}
	}

	public boolean overwriteFile(JSONArray args, CallbackContext callbackContext) {
		try {
			JSONObject params = args.getJSONObject(0);
			Uri uri = Uri.parse(params.getString("uri"));
			try (OutputStream outputStream = cordovaInterface.getContext().getContentResolver().openOutputStream(uri)) {
				outputStream.write(Base64.decode(params.getString("data"), Base64.DEFAULT));
			}
			callbackContext.success(uri.toString());
			return true;
		} catch (Exception e) {
			callbackContext.error(debugLog(e));
			return false;
		}
	}

	public boolean touchFile(JSONArray args, CallbackContext callbackContext) {
		try {
			JSONObject params = args.getJSONObject(0);
			Uri uri = Uri.parse(params.getString("uri"));
			try (
					InputStream inputStream = cordovaInterface.getContext().getContentResolver()
							.openInputStream(uri);
					ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
				FileUtils.copy(inputStream, byteArrayOutputStream);
				try (OutputStream outputStream = cordovaInterface.getContext().getContentResolver().openOutputStream(uri)) {
					outputStream.write(byteArrayOutputStream.toByteArray());
				}
				callbackContext.success(uri.toString());
				return true;
			} catch (Exception e) {
				callbackContext.error(debugLog(e));
				return false;
			}

		} catch (Exception e) {
			callbackContext.error(debugLog(e));
			return false;
		}
	}

	public boolean saveFile(JSONArray args, CallbackContext callbackContext) {
		try {
			JSONObject params = args.getJSONObject(0);
			saveFileData.put(callbackContext.getCallbackId(), params.getString("data"));
			Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
			intent.addCategory(Intent.CATEGORY_OPENABLE);
			String filename = null;
			try {
				if (!params.isNull("filename"))
					filename = params.getString("filename");
			} catch (Exception e) {
				debugLog(e);
			}
			if (filename != null) {
				intent.setType(MimeTypeMap.getSingleton()
						.getMimeTypeFromExtension(filename.substring(filename.lastIndexOf('.') + 1)));
				intent.putExtra(Intent.EXTRA_TITLE, filename);
			}
			String folder = null;
			try {
				if (!params.isNull("folder"))
					folder = params.getString("folder");
			} catch (Exception e) {
				debugLog(e);
			}
			if (folder != null)
				intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, folder);
			this.callbackContext = callbackContext;
			cordovaInterface.startActivityForResult(this, intent, Action.saveFile.ordinal());
			return true;
		} catch (Exception e) {
			callbackContext.error(debugLog(e));
			return false;
		}
	}

	public boolean deleteFile(JSONArray args, CallbackContext callbackContext) {
		try {
			callbackContext.success(
					cordovaInterface.getContext().getContentResolver().delete(Uri.parse(args.getString(0)), null));
			return true;
		} catch (Exception e) {
			callbackContext.error(debugLog(e));
			return false;
		}
	}

	public boolean getFileName(JSONArray args, CallbackContext callbackContext) {
		try (Cursor cursor = cordovaInterface.getContext().getContentResolver().query(Uri.parse(args.getString(0)),
				null, null, null, null)) {
			if (cursor != null && cursor.moveToFirst()) {
				callbackContext.success(cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)));
				return true;
			}
			callbackContext.error(args.getString(0) + " not found");
			return false;
		} catch (Exception e) {
			callbackContext.error(debugLog(e));
			return false;
		}
	}

	public boolean getUri(JSONArray args, CallbackContext callbackContext) {
		try {
			JSONObject params = args.getJSONObject(0);
			String folder = null;
			try {
				if (!params.isNull("folder"))
					params.getString("folder");
			} catch (Exception e) {
				debugLog(e);
			}
			DocumentFile documentFile = DocumentFile.fromTreeUri(
					cordovaInterface.getContext(),
					Uri.parse(folder));
			String subFolder = null;
			try {
				if (!params.isNull("subFolder"))
					subFolder = params.getString("subFolder");
			} catch (Exception e) {
				debugLog(e);
			}
			if (subFolder != null) {
				String subFolders[] = subFolder.split("/");
				for (int i = 0; i < subFolders.length; ++i) {
					DocumentFile subFolderDocumentFile = null;
					for (DocumentFile subFile : documentFile.listFiles()) {
						if (subFile.isDirectory() && subFile.getName().equals(subFolder)) {
							subFolderDocumentFile = subFile;
							break;
						}
					}
					if (subFolderDocumentFile == null)
						throw new Exception("Subfolder not found in " + folder + ": " + subFolder);
					documentFile = subFolderDocumentFile;
				}
			}
			String filename = null;
			try {
				if (!params.isNull("filename"))
					filename = params.getString("filename");
			} catch (Exception e) {
				debugLog(e);
			}
			if (filename == null) {
				callbackContext.success(documentFile.getUri().toString());
				return true;
			}
			DocumentFile file = null;
			for (DocumentFile subFile : documentFile.listFiles()) {
				if (!subFile.isDirectory() && subFile.getName().equals(filename)) {
					file = subFile;
					break;
				}
			}
			if (file == null)
				throw new Exception("File not found in " + subFolder + " of " + folder + ": " + filename);
			callbackContext.success(file.getUri().toString());
			return true;
		} catch (Exception e) {
			callbackContext.error(debugLog(e));
			return false;
		}
	}

	public boolean existsFile(JSONArray args, CallbackContext callbackContext) {
		try {
			String filename = null;
			if (!args.isNull(0)) {
				filename = args.getString(0);
			}
			Cursor cursor = cordovaInterface.getContext().getContentResolver()
					.query(MediaStore.Files.getContentUri("external"), null, null, null, null);
			if (cursor.moveToFirst()) {
				do {
					if (cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)).equals(filename)) {
						callbackContext.success(ContentUris
								.withAppendedId(MediaStore.Files.getContentUri("external"),
										cursor.getInt(cursor.getColumnIndex(MediaStore.Images.ImageColumns._ID)))
								.toString());
						return true;
					}
				} while (cursor.moveToNext());
			}
		} catch (Exception e) {
			callbackContext.error(debugLog(e));
			return false;
		}
		callbackContext.success(-1);
		return false;
	}

	public boolean getDataForOpenFile(JSONArray args, CallbackContext callbackContext) {
		try {
			String uri = null;
			if (!args.isNull(0)) {
				uri = args.getString(0);
			}
			// String[] realUri = this.getRealPathFromURI(Uri.parse(uri));
			// JSONObject response = new JSONObject()
			//		.put("uri", realUri[0])
			//		.put("mimeType",
			//				MimeTypeMap.getSingleton()
			//						.getMimeTypeFromExtension(realUri[0].substring(uri.lastIndexOf('.') + 1)))
			//		.put("filename", realUri[1]);

			String[] info = this.getInfoFromUri(Uri.parse(uri));
			// ContentResolver contentResolver = cordovaInterface.getContext().getContentResolver();
			JSONObject response = new JSONObject().put("mimeType", info[0]).put("filename", info[1]);
			callbackContext.success(response);
			return true;
		} catch (Exception e) {
			callbackContext.error(debugLog(e));
			return false;
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		if (callbackContext == null) {
			debugLog("callbackContext==null in onActivityResult");
			return;
		}
		if (requestCode < 0 || requestCode >= Action.values().length) {
			callbackContext.error(debugLog("Invalid request code: " + requestCode));
			return;
		}
		switch (Action.values()[requestCode]) {
			case selectFolder:
			case selectFile:
				if (resultCode != Activity.RESULT_OK) {
					callbackContext.error(debugLog("Cancelled"));
					return;
				}
				callbackContext.success(intent.getDataString());
				break;
			case openFolder:
			case openFile:
				callbackContext.success();
				break;
			case saveFile:
				if (resultCode != Activity.RESULT_OK) {
					callbackContext.error(debugLog("Cancelled"));
					return;
				}
				String data = saveFileData.remove(callbackContext.getCallbackId());
				if (data == null) {
					callbackContext.error(debugLog("No saveFileData in onActivityResult"));
					break;
				}

				this.overwriteDocument(intent.getData(), data, intent);
				/*
				try (OutputStream outputStream = cordovaInterface.getContext().getContentResolver()
						.openOutputStream(intent.getData())) {
					outputStream.write(Base64.decode(data, Base64.DEFAULT));
					callbackContext.success(intent.getDataString());
				} catch (Exception e) {
					callbackContext.error(debugLog(e));
				}
				*/
				break;
			default:
				callbackContext.error(debugLog("Invalid request code: " + Action.values()[requestCode].toString()));
		}
	}

	private void overwriteDocument(Uri uri, String data, Intent intent) {
		try {
			ParcelFileDescriptor pfd = cordovaInterface.getContext().getContentResolver().openFileDescriptor(uri, "w");
			FileOutputStream fileOutputStream = new FileOutputStream(pfd.getFileDescriptor());

			// Forcer le fichier à être vidé avant d'écrire les nouvelles données
			FileChannel fChan=fileOutputStream.getChannel();
			fChan.truncate(0);

			fileOutputStream.write(Base64.decode(data, Base64.DEFAULT));
			fileOutputStream.close();
			pfd.close();

			callbackContext.success(intent.getDataString());
		} catch (Exception e) {
			callbackContext.error(debugLog(e));
		}
	}

	public String debugLog(Throwable throwable) {
		try {
			StringWriter stringWriter = new StringWriter();
			PrintWriter printWriter = new PrintWriter(stringWriter);
			throwable.printStackTrace(printWriter);
			String stackTrace = stringWriter.toString();
			Log.d(throwable.getLocalizedMessage(), stackTrace, throwable);
			cordovaWebView.getEngine().evaluateJavascript(
					"console.log('" + stackTrace.replace(
							"'",
							"\\'").replace(
									"\n",
									"\\n")
							.replace(
									"\t",
									"\\t")
							+ "');",
					this);
			printWriter.close();
			stringWriter.close();
			return stackTrace;
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
	}

	public String debugLog(String message) {
		Log.d(getClass().getName(), message);
		cordovaWebView.getEngine().evaluateJavascript(
				"console.log('" + message.replace(
						"'",
						"\\'").replace(
								"\n",
								"\\n")
						.replace(
								"\t",
								"\\t")
						+ "');",
				this);
		return message;
	}

	// public String[] getRealPathFromURI(Uri contentUri) {
	//	Cursor cursor = null;
	//	try {
	//		cursor = cordovaInterface.getContext().getContentResolver().query(contentUri, null, null, null, null);
	//		int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
	//		cursor.moveToFirst();
	//		String[] response = {
	//				cursor.getString(column_index),
	//				cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
	//		};
	//		return response;
	//	} finally {
	//		if (cursor != null) {
	//			cursor.close();
	//		}
	//	}
	// }

	public String[] getInfoFromUri(Uri contentUri) {
		Cursor cursor = null;
		try {
			cursor = cordovaInterface.getContext().getContentResolver().query(contentUri, null, null, null, null);			
			cursor.moveToFirst();
			String[] response = {
					cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.MIME_TYPE)),
					cursor.getString(cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME))
			};
			return response;
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
	}

	@Override
	public void onReceiveValue(String value) {
	}
}
