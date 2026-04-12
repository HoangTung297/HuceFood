package com.example.foodorder.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import com.example.foodorder.model.Food;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "FoodOrder.db";
    private static final int DATABASE_VERSION = 1;

    // Table Users
    private static final String TABLE_USERS = "users";
    private static final String COLUMN_USER_ID = "user_id";
    private static final String COLUMN_USERNAME = "username";
    private static final String COLUMN_EMAIL = "email";
    private static final String COLUMN_PASSWORD = "password";
    private static final String COLUMN_PHONE = "phone";

    // Table Foods
    private static final String TABLE_FOODS = "foods";
    private static final String COLUMN_FOOD_ID = "food_id";
    private static final String COLUMN_FOOD_NAME = "food_name";
    private static final String COLUMN_DESCRIPTION = "description";
    private static final String COLUMN_PRICE = "price";
    private static final String COLUMN_CATEGORY = "category";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create Users table
        String createUsersTable = "CREATE TABLE " + TABLE_USERS + "("
                + COLUMN_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_USERNAME + " TEXT,"
                + COLUMN_EMAIL + " TEXT UNIQUE,"
                + COLUMN_PASSWORD + " TEXT,"
                + COLUMN_PHONE + " TEXT" + ")";
        db.execSQL(createUsersTable);

        // Create Foods table
        String createFoodsTable = "CREATE TABLE " + TABLE_FOODS + "("
                + COLUMN_FOOD_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_FOOD_NAME + " TEXT,"
                + COLUMN_DESCRIPTION + " TEXT,"
                + COLUMN_PRICE + " REAL,"
                + COLUMN_CATEGORY + " TEXT" + ")";
        db.execSQL(createFoodsTable);

        // Thêm tài khoản mặc định
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_USERNAME, "tung");
        cv.put(COLUMN_EMAIL, "tung@gmail.com");
        cv.put(COLUMN_PASSWORD, "123456");
        cv.put(COLUMN_PHONE, "0123456789");
        db.insert(TABLE_USERS, null, cv);

        // Thêm món ăn mẫu
        insertSampleFoods(db);
    }

    private void insertSampleFoods(SQLiteDatabase db) {
        ContentValues cv = new ContentValues();

        // Fast Food
        cv.put(COLUMN_FOOD_NAME, "Hamburger");
        cv.put(COLUMN_DESCRIPTION, "Bánh mì kẹp thịt bò, rau xà lách, cà chua");
        cv.put(COLUMN_PRICE, 35000);
        cv.put(COLUMN_CATEGORY, "Fast Food");
        db.insert(TABLE_FOODS, null, cv);

        cv.clear();
        cv.put(COLUMN_FOOD_NAME, "Pizza");
        cv.put(COLUMN_DESCRIPTION, "Pizza hải sản phô mai");
        cv.put(COLUMN_PRICE, 120000);
        cv.put(COLUMN_CATEGORY, "Fast Food");
        db.insert(TABLE_FOODS, null, cv);

        // Vietnamese Food
        cv.clear();
        cv.put(COLUMN_FOOD_NAME, "Phở bò");
        cv.put(COLUMN_DESCRIPTION, "Phở bò tái nạm");
        cv.put(COLUMN_PRICE, 45000);
        cv.put(COLUMN_CATEGORY, "Việt Nam");
        db.insert(TABLE_FOODS, null, cv);

        cv.clear();
        cv.put(COLUMN_FOOD_NAME, "Cơm tấm");
        cv.put(COLUMN_DESCRIPTION, "Cơm tấm sườn bì chả");
        cv.put(COLUMN_PRICE, 40000);
        cv.put(COLUMN_CATEGORY, "Việt Nam");
        db.insert(TABLE_FOODS, null, cv);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_FOODS);
        onCreate(db);
    }

    // ============= USER METHODS =============

    public boolean registerUser(String username, String email, String password, String phone) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_USERNAME, username);
        cv.put(COLUMN_EMAIL, email);
        cv.put(COLUMN_PASSWORD, password);
        cv.put(COLUMN_PHONE, phone);

        long result = db.insert(TABLE_USERS, null, cv);
        db.close();
        return result != -1;
    }

    public boolean checkUser(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_USERS + " WHERE "
                + COLUMN_EMAIL + " = ? AND " + COLUMN_PASSWORD + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{email, password});
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        db.close();
        return exists;
    }

    // THÊM PHƯƠNG THỨC NÀY - Kiểm tra email tồn tại
    public boolean isEmailExists(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_USERS + " WHERE " + COLUMN_EMAIL + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{email});
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        db.close();
        return exists;
    }

    // THÊM PHƯƠNG THỨC NÀY - In ra tất cả user (để debug)
    public void printAllUsers() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_USERS, null);

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_USER_ID));
                String username = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USERNAME));
                String email = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EMAIL));
                String password = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PASSWORD));
                String phone = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PHONE));

                Log.d("DatabaseHelper", "User: ID=" + id + ", Name=" + username +
                        ", Email=" + email + ", Pass=" + password + ", Phone=" + phone);
            } while (cursor.moveToNext());
        } else {
            Log.d("DatabaseHelper", "No users found");
        }
        cursor.close();
        db.close();
    }

    // ============= FOOD METHODS =============

    public List<Food> getAllFoods() {
        List<Food> foodList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_FOODS, null);

        if (cursor.moveToFirst()) {
            do {
                Food food = new Food(
                        cursor.getInt(0),
                        cursor.getString(1),
                        cursor.getString(2),
                        cursor.getDouble(3),
                        0,
                        cursor.getString(4)
                );
                foodList.add(food);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return foodList;
    }

    public List<Food> getFoodsByCategory(String category) {
        List<Food> foodList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_FOODS + " WHERE "
                + COLUMN_CATEGORY + " = ?", new String[]{category});

        if (cursor.moveToFirst()) {
            do {
                Food food = new Food(
                        cursor.getInt(0),
                        cursor.getString(1),
                        cursor.getString(2),
                        cursor.getDouble(3),
                        0,
                        cursor.getString(4)
                );
                foodList.add(food);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return foodList;
    }
}