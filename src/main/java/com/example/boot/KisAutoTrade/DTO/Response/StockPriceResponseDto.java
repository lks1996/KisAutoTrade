package com.example.boot.KisAutoTrade.DTO.Response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class StockPriceResponseDto {

    @JsonProperty("iscd_stat_cls_code")
    private String iscdStatClsCode;

    @JsonProperty("mrkt_cls_code")
    private String mrktClsCode;

    @JsonProperty("shrn_iscd")
    private String shrnIscd;

    @JsonProperty("hts_kor_isnm")
    private String htsKorIsnm;

    @JsonProperty("temp_stop_yn")
    private String tempStopYn;

    @JsonProperty("stck_prpr")
    private String stckPrpr;

    @JsonProperty("prdy_vrss")
    private String prdyVrss;

    @JsonProperty("prdy_vrss_sign")
    private String prdyVrssSign;

    @JsonProperty("prdy_ctrt")
    private String prdyCtrt;

    @JsonProperty("acml_tr_pbmn")
    private String acmlTrPbmn;

    @JsonProperty("acml_vol")
    private String acmlVol;

    @JsonProperty("prdy_vrss_vol_rate")
    private String prdyVrssVolRate;

    @JsonProperty("stck_oprc")
    private String stckOprc;

    @JsonProperty("stck_hgpr")
    private String stckHgpr;

    @JsonProperty("stck_lwpr")
    private String stckLwpr;

    @JsonProperty("stck_mxpr")
    private String stckMxpr;

    @JsonProperty("stck_llam")
    private String stckLlam;

    @JsonProperty("stck_sdpr")
    private String stckSdpr;

    @JsonProperty("wghn_avrg_stck_prc")
    private String wghnAvrgStckPrc;

    @JsonProperty("hts_frgn_ehrt")
    private String htsFrgnEhrt;

    @JsonProperty("frgn_ntby_qty")
    private String frgnNtbyQty;

    @JsonProperty("pgtr_ntby_qty")
    private String pgtrNtbyQty;

    @JsonProperty("pvt_scnd_dmrs_prc")
    private String pvtScndDmrsPrc;

    @JsonProperty("pvt_frst_dmrs_prc")
    private String pvtFrstDmrsPrc;

    @JsonProperty("pvt_pont_val")
    private String pvtPontVal;

    @JsonProperty("dmrs_val")
    private String dmrsVal;

    @JsonProperty("bps")
    private String bps;

    @JsonProperty("per")
    private String per;

    @JsonProperty("pbr")
    private String pbr;

    @JsonProperty("eps")
    private String eps;

    @JsonProperty("divd_rate")
    private String divdRate;

    @JsonProperty("askp")
    private String askp;

    @JsonProperty("bidp")
    private String bidp;

    @JsonProperty("vol_tnrt")
    private String volTnrt;

    @JsonProperty("invst_tgt_cls")
    private String invstTgtCls;

    @JsonProperty("short_over_yn")
    private String shortOverYn;

    @JsonProperty("oprc_hrng_rang_tp_code")
    private String oprcHrngRangTpCode;

    @JsonProperty("oprc_hrng_rang_rt")
    private String oprcHrngRangRt;

    @JsonProperty("last_ssts_cntg_qty")
    private String lastSstsCntgQty;

    @JsonProperty("invt_caful_yn")
    private String invtCafulYn;

    @JsonProperty("mrkt_warn_cls_code")
    private String mrktWarnClsCode;

    @JsonProperty("short_over_cntg_qty")
    private String shortOverCntgQty;

    @JsonProperty("sltr_askp")
    private String sltrAskp;

    @JsonProperty("sltr_bidp")
    private String sltrBidp;

    @JsonProperty("cpfn")
    private String cpfn;

    @JsonProperty("rstc_wdth_prc")
    private String rstcWdthPrc;

    @JsonProperty("stck_dvd_yld")
    private String stckDvdYld;

    @JsonProperty("epsr")
    private String epsr;

    @JsonProperty("lstn_stcn")
    private String lstnStcn;

    @JsonProperty("cpfn_ccls")
    private String cpfnCcls;

    @JsonProperty("stck_fcam")
    private String stckFcam;

    @JsonProperty("stck_sspr")
    private String stckSspr;

    @JsonProperty("aspr_unit")
    private String asprUnit;

    @JsonProperty("aspr_unit_num")
    private String asprUnitNum;

    @JsonProperty("new_hgpr_lwpr_cls_code")
    private String newHgprLwprClsCode;

    @JsonProperty("oprc_rang_hr")
    private String oprcRangHr;

    @JsonProperty("oprc_rang_lw")
    private String oprcRangLw;

    @JsonProperty("loan_rmnd_rate")
    private String loanRmndRate;

}
