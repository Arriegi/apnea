package eus.arriegi.apnea;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

/**
 * Created by lenovo on 27/12/2016.
 */

public class Sleep {
    public static final String _ID = "id";
    public static final String TABLE_NAME = "sleep";
    public static final String LEFT_TITLE = "left";
    public static final String LEFT_COUNT_TITLE = "leftcount";
    public static final String RIGHT_TITLE = "right";
    public static final String RIGHT_COUNT_TITLE = "rightcount";
    public static final String BACK_TITLE = "back";
    public static final String BACK_COUNT_TITLE = "backcount";
    public static final String STOMACH_TITLE = "stomach";
    public static final String STOMACH_COUNT_TITLE = "stomachcount";
    public static final String UP_TITLE = "up";
    public static final String UP_COUNT_TITLE = "upcount";
    public static final String TOTAL_TITLE = "total";
    public static final String TOTAL_COUNT_TITLE = "totalcount";

    long left, leftCount, right, rightCount, back, backCount, stomach, stomachCount, up, upCount,
            total, totalCount;

    public Sleep(long left, long leftCount, long right, long rightCount, long back, long backCount,
                 long stomach, long stomachCount, long up, long upCount, long total, long totalCount) {
        this.left = left;
        this.leftCount = leftCount;
        this.right = right;
        this.rightCount = rightCount;
        this.back = back;
        this.backCount = backCount;
        this.stomach = stomach;
        this.stomachCount = stomachCount;
        this.up = up;
        this.upCount = upCount;
        this.total = total;
        this.totalCount = totalCount;
    }

    public long getLeft() {
        return left;
    }

    public void setLeft(long left) {
        this.left = left;
    }

    public long getLeftCount() {
        return leftCount;
    }

    public void setLeftCount(long leftCount) {
        this.leftCount = leftCount;
    }

    public long getRight() {
        return right;
    }

    public void setRight(long right) {
        this.right = right;
    }

    public long getRightCount() {
        return rightCount;
    }

    public void setRightCount(long rightCount) {
        this.rightCount = rightCount;
    }

    public long getBack() {
        return back;
    }

    public void setBack(long back) {
        this.back = back;
    }

    public long getBackCount() {
        return backCount;
    }

    public void setBackCount(long backCount) {
        this.backCount = backCount;
    }

    public long getStomach() {
        return stomach;
    }

    public void setStomach(long stomach) {
        this.stomach = stomach;
    }

    public long getStomachCount() {
        return stomachCount;
    }

    public void setStomachCount(long stomachCount) {
        this.stomachCount = stomachCount;
    }

    public long getUp() {
        return up;
    }

    public void setUp(long up) {
        this.up = up;
    }

    public long getUpCount() {
        return upCount;
    }

    public void setUpCount(long upCount) {
        this.upCount = upCount;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public long getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(long totalCount) {
        this.totalCount = totalCount;
    }

    public long storeOnDB(SQLiteDatabase db) {
        ContentValues values = new ContentValues();
        values.put(LEFT_TITLE,left);
        values.put(LEFT_COUNT_TITLE,leftCount);
        values.put(RIGHT_TITLE,right);
        values.put(RIGHT_COUNT_TITLE,rightCount);
        values.put(BACK_TITLE,back);
        values.put(BACK_COUNT_TITLE,backCount);
        values.put(STOMACH_TITLE,stomach);
        values.put(STOMACH_COUNT_TITLE,stomachCount);
        values.put(UP_TITLE,up);
        values.put(UP_COUNT_TITLE,upCount);
        values.put(TOTAL_TITLE,total);
        values.put(TOTAL_COUNT_TITLE,totalCount);
        return db.insert(TABLE_NAME,null,values);
    }
}
