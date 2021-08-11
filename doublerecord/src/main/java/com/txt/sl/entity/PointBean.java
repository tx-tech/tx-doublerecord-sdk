package com.txt.sl.entity;

/**
 * Created by JustinWjq
 *
 * @date 2020/6/16.
 * description：
 */
public class PointBean {

    /**
     * time : 0
     * operation : mapping
     * lineArr : {"startX":0,"startY":0,"currentX":0,"currentY":0,"z":0,"colorStr":"#000"}
     */

    private int time;
    private String operation;
    private LineArrBean lineArr;

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public LineArrBean getLineArr() {
        return lineArr;
    }

    public void setLineArr(LineArrBean lineArr) {
        this.lineArr = lineArr;
    }

    public static class LineArrBean {
        /**
         * startX : 0
         * startY : 0
         * currentX : 0
         * currentY : 0
         * z : 0
         * colorStr : #000
         */

        private int startX;
        private int startY;
        private int currentX;
        private int currentY;
        private int z;
        private String colorStr;

        public int getStartX() {
            return startX;
        }

        public void setStartX(int startX) {
            this.startX = startX;
        }

        public int getStartY() {
            return startY;
        }

        public void setStartY(int startY) {
            this.startY = startY;
        }

        public int getCurrentX() {
            return currentX;
        }

        public void setCurrentX(int currentX) {
            this.currentX = currentX;
        }

        public int getCurrentY() {
            return currentY;
        }

        public void setCurrentY(int currentY) {
            this.currentY = currentY;
        }

        public int getZ() {
            return z;
        }

        public void setZ(int z) {
            this.z = z;
        }

        public String getColorStr() {
            return colorStr;
        }

        public void setColorStr(String colorStr) {
            this.colorStr = colorStr;
        }
    }
}
