package com.yada.smartpos.spos;

import java.util.List;

public class ParamDownload {

    public String version;
    // 48域
    public String termBasicParam;
    // 56域的DF29
    public String programAppParam;
    // AID列表
    public List<String> aidListParam;
    // RID列表
    public List<String> ridListParam;
}