package com.yada.smartpos.spos;

import com.yada.sdk.device.pos.posp.params.*;

import java.util.Map;

public class ParamInfo {

    private Block01 block01;
    private Block02 block02;
    private Map<String, Block03> block03Map;
    private Map<String, Block04_1> block04_1Map;
    private Map<String, Block04_2> block04_2Map;

    public Block01 getBlock01() {
        return block01;
    }

    public void setBlock01(Block01 block01) {
        this.block01 = block01;
    }

    public Block02 getBlock02() {
        return block02;
    }

    public void setBlock02(Block02 block02) {
        this.block02 = block02;
    }

    public Map<String, Block03> getBlock03Map() {
        return block03Map;
    }

    public void setBlock03Map(Map<String, Block03> block03Map) {
        this.block03Map = block03Map;
    }

    public Map<String, Block04_1> getBlock04_1Map() {
        return block04_1Map;
    }

    public void setBlock04_1Map(Map<String, Block04_1> block04_1Map) {
        this.block04_1Map = block04_1Map;
    }

    public Map<String, Block04_2> getBlock04_2Map() {
        return block04_2Map;
    }

    public void setBlock04_2Map(Map<String, Block04_2> block04_2Map) {
        this.block04_2Map = block04_2Map;
    }
}