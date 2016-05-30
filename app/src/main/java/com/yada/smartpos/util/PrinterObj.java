package com.yada.smartpos.util;

import android.graphics.Bitmap;

public class PrinterObj {

    public static final String TXT = "txt";
    public static final String BARCODE = "one-dimension";
    public static final String QRCODE = "two-dimension";
    public static final String BITMAP = "jpg";
    public static final String LINE = "line";
    public static final String FEED_LINE = "feed-line";

    public static final String LEFT = "left";
    public static final String RIGHT = "right";
    public static final String CENTER = "center";

    private String content_type;
    private String size;
    private String content;
    private String position = LEFT;
    private String offset = "0";
    private String bold = "0";
    private String height;
    private Bitmap bitmap;
    private int mOffset;

    public String getContent_type() {
        return content_type;
    }

    public String getSize() {
        return size;
    }

    public String getContent() {
        return content;
    }

    public String getPosition() {
        return position;
    }

    public String getOffset() {
        return offset;
    }

    public String getBold() {
        return bold;
    }

    public String getHeight() {
        return height;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setContent_type(String content_type) {
        this.content_type = content_type;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public void setOffset(String offset) {
        this.offset = offset;
    }

    public void setBold(String bold) {
        this.bold = bold;
    }

    public void setHeight(String height) {
        this.height = height;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public int getmOffset() {
        return mOffset;
    }

    public void setmOffset(int mOffset) {
        this.mOffset = mOffset;
    }

    public String getScript() {

        if (TXT.equals(content_type)) {
            return dealTxt();
        } else if (BARCODE.equals(content_type)) {
            return dealBarCode();
        } else if (QRCODE.equals(content_type)) {
            return dealQRCode();
        } else if (BITMAP.equals(content_type)) {
            return dealBitmap();
        } else if (LINE.equals(content_type)) {
            return dealLine();
        } else if (FEED_LINE.equals(content_type)) {
            return dealFeedLine();
        } else {
            return null;
        }

    }

    private String dealLine() {
        return "*line \n";
    }

    private String dealFeedLine() {
        try {
            int num = Integer.parseInt(content);
            return "*feedLine" + num + " \n";
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private String dealBitmap() {
        try {
            //对齐方式
            if (!LEFT.equals(position)) {
                mOffset = 0;
            }
            return BITMAP;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private String dealQRCode() {
        try {
            StringBuffer sb = new StringBuffer();
            int height = 150;
            //高
            if ("-1".equals(size) || size == null) {

            } else {
                int h = Integer.parseInt(size);
                if (0 < h && h < 384) {
                    height = h;
                } else {
                    //高度不合法
                    return null;
                }
            }

            sb.append("!qrcode " + height + " " + 2 + "\n");

            // 灰度
            if (bold.equals("1")) {
                sb.append("!gray 12\n");
            } else {
                sb.append("!gray 5\n");
            }
            String qrcode;
            //对齐方式
            if (RIGHT.equals(position)) {
                qrcode = "*qrcode r ";
            } else if (CENTER.equals(position)) {
                qrcode = "*qrcode c ";
            } else {
                qrcode = "*qrcode l ";
            }
            //
            sb.append(qrcode + content + "\n");

            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private String dealBarCode() {
        try {
            StringBuffer sb = new StringBuffer();
            int bwitdh = 2;
            int bheight = 64;
            //宽
            if ("-1".equals(size) || size == null) {

            } else {
                int bw = Integer.parseInt(size);
                if (0 < bw && bw < 9) {
                    bwitdh = bw;
                } else {
                    //宽度不合法
                    return null;
                }
            }
            //高
            if ("-1".equals(height) || height == null) {

            } else {
                int bh = Integer.parseInt(height);
                bh = bh * 8;
                if (0 > bh || bh > 320) {
                    return null;
                }
                bheight = bh;
            }

            sb.append("!barcode " + bwitdh + " " + bheight + "\n");

            // 灰度
            if (bold.equals("1")) {
                sb.append("!gray 12\n");
            } else {
                sb.append("!gray 5\n");
            }
            String barcode;
            //对齐方式
            if (RIGHT.equals(position)) {
                barcode = "*barcode r ";
            } else if (CENTER.equals(position)) {
                barcode = "*barcode c ";
            } else {
                barcode = "*barcode l ";
            }
            //
            sb.append(barcode + content + "\n");

            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static final int E_S = 1;
    private static final int E_N = 12;
    private static final int E_L = 13;

    private static final int C_S = 6;
    private static final int C_N = 1;
    private static final int C_L = 3;

    //private static final int C_B=9;
    //private static final int E_B=18;

    public enum Font {
        //B(C_B,E_B,null),
        S(C_S, E_S, null),
        N(C_N, E_N, S),
        L(C_L, E_L, S);

        private int e_size;
        private int c_size;
        private Font next;

        private Font(int c_size, int e_size, Font next) {
            this.e_size = e_size;
            this.c_size = c_size;
            this.next = next;
        }

        public int getE_size() {
            return e_size;
        }

        public int getC_size() {
            return c_size;
        }

        public Font getNext() {
            return next;
        }

    }

    private String dealTxt() {
        try {
            String text;
            StringBuffer sb = new StringBuffer();
            //大小
            Font font = Font.S;
            int e_bold = 0;
            if ("1".equals(size) || size == null) {
                font = Font.S;
            } else if ("2".equals(size)) {
                font = Font.N;
            } else if ("3".equals(size)) {
                font = Font.L;
            }

            // 灰度
            if (bold.equals("1")) {
                font = font.getNext();
                if (font == null) {
                    font = Font.S;
                }
                e_bold = 0;

            } else {
                e_bold = 3;

            }
            sb.append("!nlfont " + font.getC_size() + " " + font.getE_size() + " " + e_bold + "\n");
            // 行间距
            sb.append("!yspace 6\n");
            //对齐方式
            if (RIGHT.equals(position)) {
                text = "*text r ";
            } else if (CENTER.equals(position)) {
                text = "*text c ";
            } else {
                text = "*text l ";
            }
            if (content != null) {
                String[] split = content.split("\n");
                for (String s : split) {
                    sb.append(text + s + "\n");
                }
            }
            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}