package com.yada.smartpos.util;

import java.util.List;

public class PrinterObjs {

    List<PrinterObj> spos;

    public PrinterObjs(List<PrinterObj> spos) {
        this.spos = spos;
    }

    public List<PrinterObj> getSpos() {
        return spos;
    }

    public void setSpos(List<PrinterObj> spos) {
        this.spos = spos;
    }
}