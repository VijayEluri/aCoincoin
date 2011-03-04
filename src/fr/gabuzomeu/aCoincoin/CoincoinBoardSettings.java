package fr.gabuzomeu.aCoincoin;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import android.content.ContentValues;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.util.Log;



//From http://code.google.com/p/connectbot/source/browse/trunk/connectbot/src/org/connectbot/HostEditor.java?r=54

public class CoincoinBoardSettings extends PreferenceActivity implements OnSharedPreferenceChangeListener {

CoinCoinApp app;
	
	
	public class CursorPreferenceHack implements SharedPreferences {

        protected final SQLiteDatabase db;
        protected final String table;
        protected final int id;

        protected Map<String, String> values = new HashMap<String, String>();
        
        public CursorPreferenceHack(SQLiteDatabase db, String table, int id) {
                this.db = db;
                this.table = table;
                this.id = id;
                
                this.cacheValues();
                
        }
        
        protected void cacheValues() {
                // fill a cursor and cache the values locally
                // this makes sure we dont have any floating cursor to dispose later

                Cursor cursor = db.query(table, null, "id = ?",
                                new String[] { Integer.toString(id) }, null, null, null);
                cursor.moveToFirst();
                
                for(int i = 0; i < cursor.getColumnCount(); i++) {
                        String key = cursor.getColumnName(i);
                        String value = cursor.getString(i);
                        values.put(key, value);
                }
                
                cursor.close();
                
        }
        
        public boolean contains(String key) {
                return values.containsKey(key);
        }
        
        public class Editor implements SharedPreferences.Editor {
                
                public ContentValues update = new ContentValues();
                
                public SharedPreferences.Editor clear() {
                        Log.d(this.getClass().toString(), "clear()");
                        update = new ContentValues();
                        return this;
                }

                public boolean commit() {
                        Log.d(this.getClass().toString(), "commit() changes back to database");
                        db.update(table, update, "id = ?", new String[] { Integer.toString(id) });
                        
                        // make sure we refresh the parent cached values
                        cacheValues();
                        
                        // and update any listeners
                        for(OnSharedPreferenceChangeListener listener : listeners) {
                                listener.onSharedPreferenceChanged(CursorPreferenceHack.this, null);
                        }
                        app.setBoardsListUpdated( true);
                        return true;
                }

                public android.content.SharedPreferences.Editor putBoolean(String key, boolean value) {
                        return this.putString(key, Boolean.toString(value));
                }

                public android.content.SharedPreferences.Editor putFloat(String key, float value) {
                        return this.putString(key, Float.toString(value));
                }

                public android.content.SharedPreferences.Editor putInt(String key, int value) {
                        return this.putString(key, Integer.toString(value));
                }

                public android.content.SharedPreferences.Editor putLong(String key, long value) {
                        return this.putString(key, Long.toString(value));
                }

                public android.content.SharedPreferences.Editor putString(String key, String value) {
                        Log.d(this.getClass().toString(), String.format("Editor.putString(key=%s, value=%s)", key, value));
                        update.put(key, value);
                        return this;
                }

                public android.content.SharedPreferences.Editor remove(String key) {
                        Log.d(this.getClass().toString(), String.format("Editor.remove(key=%s)", key));
                        update.remove(key);
                        return this;
                }
                
        }


        
        public Editor edit() {
                Log.d(this.getClass().toString(), "edit()");
                return new Editor();
        }

        public Map<String, ?> getAll() {
                return values;
        }

        public boolean getBoolean(String key, boolean defValue) {
                return Boolean.valueOf(this.getString(key, Boolean.toString(defValue)));
        }

        public float getFloat(String key, float defValue) {
                return Float.valueOf(this.getString(key, Float.toString(defValue)));
        }

        public int getInt(String key, int defValue) {
                return Integer.valueOf(this.getString(key, Integer.toString(defValue)));
        }

        public long getLong(String key, long defValue) {
                return Long.valueOf(this.getString(key, Long.toString(defValue)));
        }

        public String getString(String key, String defValue) {
                Log.d(this.getClass().toString(), String.format("getString(key=%s, defValue=%s)", key, defValue));
                
                if(!values.containsKey(key)) return defValue;
                return values.get(key);
        }
        
        public List<OnSharedPreferenceChangeListener> listeners = new LinkedList<OnSharedPreferenceChangeListener>();

        public void registerOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener) {
                listeners.add(listener);
        }

        public void unregisterOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener) {
                listeners.remove(listener);
        }
        
}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	protected CursorPreferenceHack pref;

	
	@Override
	public SharedPreferences getSharedPreferences(String name, int mode) {
		Log.d(this.getClass().toString(), String.format("getSharedPreferences(name=%s)", name));
		return this.pref;
	}
	
	
	
    public void onCreate(Bundle icicle) {
            super.onCreate(icicle);
           // 
            app = (CoinCoinApp)this.getApplication();
            //HostDatabase db = new HostDatabase(this);
           // int id = this.getIntent().getIntExtra(Intent.EXTRA_TITLE, -1);
            
            // TODO: we could pass through a specific ContentProvider uri here
            //this.getPreferenceManager().setSharedPreferencesName(uri);
            int boardId =  getIntent().getExtras().getInt( "board");
            this.pref = new CursorPreferenceHack( app.db , "boards", boardId);
            this.pref.registerOnSharedPreferenceChangeListener(this);
            
            this.addPreferencesFromResource(R.xml.boardsettings);
            
            this.updateSummaries();
            
            
    }
	
	
	public void updateSummaries() {
        // for all text preferences, set hint as current database value
        for(String key : this.pref.values.keySet()) {
                Preference pref = this.findPreference(key);
                if(pref == null) continue;
                if(pref instanceof CheckBoxPreference) continue;
                pref.setSummary(this.pref.getString(key, ""));
        }
        
}
	
	 public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
         // update values on changed preference
         this.updateSummaries();
         
 }

	
}
