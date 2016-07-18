package com.yada.smartpos;

import com.payneteasy.tlv.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class TLVUtilsTest {

    public static void main(String[] args) throws IOException {

        BerTlvParser parser = new BerTlvParser();

        String field56 = "DF2681C09F0607A0000000031010DF250803201206291652479F0608A000000333010103DF250803201206291655109F0607A0000000651010DF250803201206291657249F0606A00000002501DF250803201206291658039F0608A000000333010106DF250803201305171631259F0607A0000000041010DF250803201506111637409F0607A0000000032010DF250803201506171334219F0608A000000333010101DF250803201510150918539F0608A000000333010102DF25080320151015092022DF270100";
        BerTlvs tlv56 = parser.parse(HexUtil.parseHex(field56));
        Map<String, String> params = new HashMap<String, String>();
        BerTlv tlvDF26 = tlv56.find(new BerTag(0xdf, 0x26));
        BerTlvs tlvDf26 = parser.parse(tlvDF26.getBytesValue());
        for (BerTlv tlv : tlvDf26.getList()){
            params.put(tlv.getTag().toString(), tlv.getHexValue());
        }
        System.out.println(params.toString());

        String aidStr = "9F0607A0000000031010DF0101019F090214E9DF1105D84000A800DF1205D84004F800DF130500100000009F1B0400000000DF150400000000DF160100DF170100DF180103DF14029F379F3501229F15028398DF250803201206291652479F7B06000000000000DF4006000000000000DF2006000000000000DF2106000000000000DF41050000000000DF42050000000000DF43050000000000";
        BerTlvs tlvAids = parser.parse(HexUtil.parseHex(aidStr));
        for (BerTlv tlv : tlvAids.getList()){
            System.out.println(tlv.getTag().toString() + "->" + tlv.getHexValue());
        }
        System.out.println("--------------------");

        String ridStr = "9F0605A0000000659F220109DF05083230353031323331DF060101DF070101DF028180B72A8FEF5B27F2B550398FDCC256F714BAD497FF56094B7408328CB626AA6F0E6A9DF8388EB9887BC930170BCC1213E90FC070D52C8DCD0FF9E10FAD36801FE93FC998A721705091F18BC7C98241CADC15A2B9DA7FB963142C0AB640D5D0135E77EBAE95AF1B4FEFADCF9C012366BDDA0455C1564A68810D7127676D493890BDDF040103DF03144410C6D51C2F83ADFD92528FA6E38A32DF048D0A";
        BerTlvs tlvRids = parser.parse(HexUtil.parseHex(ridStr));
        for (BerTlv tlv : tlvRids.getList()){
            System.out.println(tlv.getTag().toString() + "->" + tlv.getHexValue());
        }

        BerTlvBuilder builder = new BerTlvBuilder();
        BerTlv tlv = new BerTlv(new BerTag(0xdf, 0x25), HexUtil.parseHex("1234"));
        builder.addBerTlv(tlv);
        System.out.println(HexUtil.toHexString(builder.buildArray()));

    }
}
