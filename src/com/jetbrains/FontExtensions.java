/*
 * Copyright 2000-2024 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jetbrains;

import java.awt.*;
import java.util.*;

/**
 * Font-related utilities.
 */
@Service
@Provided
public interface FontExtensions {

    /**
     * The list of all supported features. For feature's description look at
     * <a href=https://learn.microsoft.com/en-us/typography/opentype/spec/featurelist>documentation</a> <br>
     * {@code kern}, {@code liga} and {@code calt} features are missing intentionally.
     * These features will be added automatically by adding {@link java.awt.font.TextAttribute} to {@link java.awt.Font}:
     * <ul>
     * <li>Attribute {@link java.awt.font.TextAttribute#KERNING} manages {@code kern} feature</li>
     * <li>Attribute {@link java.awt.font.TextAttribute#LIGATURES} manages {@code liga} and {@code calt} features</li>
     * </ul>
     */
    final class FeatureTag {
        private FeatureTag() {}
        /**<a href=https://learn.microsoft.com/en-us/typography/opentype/spec/features_ae#aalt>aalt</a>*/ public static final String AALT = "aalt";
        /**<a href=https://learn.microsoft.com/en-us/typography/opentype/spec/features_ae#abvf>abvf</a>*/ public static final String ABVF = "abvf";
        /**<a href=https://learn.microsoft.com/en-us/typography/opentype/spec/features_ae#abvm>abvm</a>*/ public static final String ABVM = "abvm";
        /**<a href=https://learn.microsoft.com/en-us/typography/opentype/spec/features_ae#abvs>abvs</a>*/ public static final String ABVS = "abvs";
        /**<a href=https://learn.microsoft.com/en-us/typography/opentype/spec/features_ae#afrc>afrc</a>*/ public static final String AFRC = "afrc";
        /**<a href=https://learn.microsoft.com/en-us/typography/opentype/spec/features_ae#akhn>akhn</a>*/ public static final String AKHN = "akhn";
        /**<a href=https://learn.microsoft.com/en-us/typography/opentype/spec/features_ae#blwf>blwf</a>*/ public static final String BLWF = "blwf";
        /**<a href=https://learn.microsoft.com/en-us/typography/opentype/spec/features_ae#blwm>blwm</a>*/ public static final String BLWM = "blwm";
        /**<a href=https://learn.microsoft.com/en-us/typography/opentype/spec/features_ae#blws>blws</a>*/ public static final String BLWS = "blws";
        /**<a href=https://learn.microsoft.com/en-us/typography/opentype/spec/features_ae#case>case</a>*/ public static final String CASE = "case";
        /**<a href=https://learn.microsoft.com/en-us/typography/opentype/spec/features_ae#ccmp>ccmp</a>*/ public static final String CCMP = "ccmp";
        /**<a href=https://learn.microsoft.com/en-us/typography/opentype/spec/features_ae#cfar>cfar</a>*/ public static final String CFAR = "cfar";
        /**<a href=https://learn.microsoft.com/en-us/typography/opentype/spec/features_ae#chws>chws</a>*/ public static final String CHWS = "chws";
        /**<a href=https://learn.microsoft.com/en-us/typography/opentype/spec/features_ae#cjct>cjct</a>*/ public static final String CJCT = "cjct";
        /**<a href=https://learn.microsoft.com/en-us/typography/opentype/spec/features_ae#clig>clig</a>*/ public static final String CLIG = "clig";
        /**<a href=https://learn.microsoft.com/en-us/typography/opentype/spec/features_ae#cpct>cpct</a>*/ public static final String CPCT = "cpct";
        /**<a href=https://learn.microsoft.com/en-us/typography/opentype/spec/features_ae#cpsp>cpsp</a>*/ public static final String CPSP = "cpsp";
        /**<a href=https://learn.microsoft.com/en-us/typography/opentype/spec/features_ae#cswh>cswh</a>*/ public static final String CSWH = "cswh";
        /**<a href=https://learn.microsoft.com/en-us/typography/opentype/spec/features_ae#curs>curs</a>*/ public static final String CURS = "curs";
        /**<a href=https://learn.microsoft.com/en-us/typography/opentype/spec/features_ae#cv01>cv01</a>*/ public static final String CV01 = "cv01";
        /**<a href=https://learn.microsoft.com/en-us/typography/opentype/spec/features_ae#cv02>cv02</a>*/ public static final String CV02 = "cv02";
        /**<a href=https://learn.microsoft.com/en-us/typography/opentype/spec/features_ae#cv03>cv03</a>*/ public static final String CV03 = "cv03";
        /**<a href=https://learn.microsoft.com/en-us/typography/opentype/spec/features_ae#cv04>cv04</a>*/ public static final String CV04 = "cv04";
        /**<a href=https://learn.microsoft.com/en-us/typography/opentype/spec/features_ae#cv05>cv05</a>*/ public static final String CV05 = "cv05";
        /**<a href=https://learn.microsoft.com/en-us/typography/opentype/spec/features_ae#cv06>cv06</a>*/ public static final String CV06 = "cv06";
        /**<a href=https://learn.microsoft.com/en-us/typography/opentype/spec/features_ae#cv07>cv07</a>*/ public static final String CV07 = "cv07";
        /**<a href=https://learn.microsoft.com/en-us/typography/opentype/spec/features_ae#cv08>cv08</a>*/ public static final String CV08 = "cv08";
        /**<a href=https://learn.microsoft.com/en-us/typography/opentype/spec/features_ae#cv09>cv09</a>*/ public static final String CV09 = "cv09";
        /**<a href=https://learn.microsoft.com/en-us/typography/opentype/spec/features_ae#cv10>cv10</a>*/ public static final String CV10 = "cv10";
        /**<a href=https://learn.microsoft.com/en-us/typography/opentype/spec/features_ae#cv11>cv11</a>*/ public static final String CV11 = "cv11";
        /**<a href=https://learn.microsoft.com/en-us/typography/opentype/spec/features_ae#cv12>cv12</a>*/ public static final String CV12 = "cv12";
        /**<a href=https://learn.microsoft.com/en-us/typography/opentype/spec/features_ae#cv13>cv13</a>*/ public static final String CV13 = "cv13";
        /**<a href=https://learn.microsoft.com/en-us/typography/opentype/spec/features_ae#cv14>cv14</a>*/ public static final String CV14 = "cv14";
        /**<a href=https://learn.microsoft.com/en-us/typography/opentype/spec/features_ae#cv15>cv15</a>*/ public static final String CV15 = "cv15";
        /**<a href=https://learn.microsoft.com/en-us/typography/opentype/spec/features_ae#cv16>cv16</a>*/ public static final String CV16 = "cv16";
        /**<a href=https://learn.microsoft.com/en-us/typography/opentype/spec/features_ae#cv17>cv17</a>*/ public static final String CV17 = "cv17";
        /**<a href=https://learn.microsoft.com/en-us/typography/opentype/spec/features_ae#cv18>cv18</a>*/ public static final String CV18 = "cv18";
        /**<a href=https://learn.microsoft.com/en-us/typography/opentype/spec/features_ae#cv19>cv19</a>*/ public static final String CV19 = "cv19";
        /**<a href=https://learn.microsoft.com/en-us/typography/opentype/spec/features_ae#cv20>cv20</a>*/ public static final String CV20 = "cv20";
        /**<a href=https://learn.microsoft.com/en-us/typography/opentype/spec/features_ae#cv21>cv21</a>*/ public static final String CV21 = "cv21";
        /**<a href=https://learn.microsoft.com/en-us/typography/opentype/spec/features_ae#cv22>cv22</a>*/ public static final String CV22 = "cv22";
        /**<a href=https://learn.microsoft.com/en-us/typography/opentype/spec/features_ae#cv23>cv23</a>*/ public static final String CV23 = "cv23";
        /**<a href=https://learn.microsoft.com/en-us/typography/opentype/spec/features_ae#cv24>cv24</a>*/ public static final String CV24 = "cv24";
        /**<a href=https://learn.microsoft.com/en-us/typography/opentype/spec/features_ae#cv25>cv25</a>*/ public static final String CV25 = "cv25";
        /**<a href=https://learn.microsoft.com/en-us/typography/opentype/spec/features_ae#cv26>cv26</a>*/ public static final String CV26 = "cv26";
        /**<a href=https://learn.microsoft.com/en-us/typography/opentype/spec/features_ae#cv27>cv27</a>*/ public static final String CV27 = "cv27";
        /**<a href=https://learn.microsoft.com/en-us/typography/opentype/spec/features_ae#cv28>cv28</a>*/ public static final String CV28 = "cv28";
        /**<a href=https://learn.microsoft.com/en-us/typography/opentype/spec/features_ae#cv29>cv29</a>*/ public static final String CV29 = "cv29";
        /**<a href=https://learn.microsoft.com/en-us/typography/opentype/spec/features_ae#cv30>cv30</a>*/ public static final String CV30 = "cv30";
        /**<a href=https://learn.microsoft.com/en-us/typography/opentype/spec/features_ae#cv31>cv31</a>*/ public static final String CV31 = "cv31";
        /**<a href=https://learn.microsoft.com/en-us/typography/opentype/spec/features_ae#cv32>cv32</a>*/ public static final String CV32 = "cv32";
        /**<a href=https://learn.microsoft.com/en-us/typography/opentype/spec/features_ae#cv33>cv33</a>*/ public static final String CV33 = "cv33";
        /**<a href=https://learn.microsoft.com/en-us/typography/opentype/spec/features_ae#cv34>cv34</a>*/ public static final String CV34 = "cv34";
        /**<a href=https://learn.microsoft.com/en-us/typography/opentype/spec/features_ae#cv35>cv35</a>*/ public static final String CV35 = "cv35";
        /**<a href=https://learn.microsoft.com/en-us/typography/opentype/spec/features_ae#cv36>cv36</a>*/ public static final String CV36 = "cv36";
        /**<a href=https://learn.microsoft.com/en-us/typography/opentype/spec/features_ae#cv37>cv37</a>*/ public static final String CV37 = "cv37";
        /**<a href=https://learn.microsoft.com/en-us/typography/opentype/spec/features_ae#cv38>cv38</a>*/ public static final String CV38 = "cv38";
        /**<a href=https://learn.microsoft.com/en-us/typography/opentype/spec/features_ae#cv39>cv39</a>*/ public static final String CV39 = "cv39";
        /**<a href=https://learn.microsoft.com/en-us/typography/opentype/spec/features_ae#cv40>cv40</a>*/ public static final String CV40 = "cv40";
        /**<a href=https://learn.microsoft.com/en-us/typography/opentype/spec/features_ae#cv41>cv41</a>*/ public static final String CV41 = "cv41";
        /**<a href=https://learn.microsoft.com/en-us/typography/opentype/spec/features_ae#cv42>cv42</a>*/ public static final String CV42 = "cv42";
        /**<a href=https://learn.microsoft.com/en-us/typography/opentype/spec/features_ae#cv43>cv43</a>*/ public static final String CV43 = "cv43";
        /**<a href=https://learn.microsoft.com/en-us/typography/opentype/spec/features_ae#cv44>cv44</a>*/ public static final String CV44 = "cv44";
        /**<a href=https://learn.microsoft.com/en-us/typography/opentype/spec/features_ae#cv45>cv45</a>*/ public static final String CV45 = "cv45";
        /**<a href=https://learn.microsoft.com/en-us/typography/opentype/spec/features_ae#cv46>cv46</a>*/ public static final String CV46 = "cv46";
        /**<a href=https://learn.microsoft.com/en-us/typography/opentype/spec/features_ae#cv47>cv47</a>*/ public static final String CV47 = "cv47";
        /**<a href=https://learn.microsoft.com/en-us/typography/opentype/spec/features_ae#cv48>cv48</a>*/ public static final String CV48 = "cv48";
        /**<a href=https://learn.microsoft.com/en-us/typography/opentype/spec/features_ae#cv49>cv49</a>*/ public static final String CV49 = "cv49";
        /**<a href=https://learn.microsoft.com/en-us/typography/opentype/spec/features_ae#cv50>cv50</a>*/ public static final String CV50 = "cv50";
        /**<a href=https://learn.microsoft.com/en-us/typography/opentype/spec/features_ae#cv51>cv51</a>*/ public static final String CV51 = "cv51";
        /**<a href=https://learn.microsoft.com/en-us/typography/opentype/spec/features_ae#cv52>cv52</a>*/ public static final String CV52 = "cv52";
        /**<a href=https://learn.microsoft.com/en-us/typography/opentype/spec/features_ae#cv53>cv53</a>*/ public static final String CV53 = "cv53";
        /**<a href=https://learn.microsoft.com/en-us/typography/opentype/spec/features_ae#cv54>cv54</a>*/ public static final String CV54 = "cv54";
        /**<a href=https://learn.microsoft.com/en-us/typography/opentype/spec/features_ae#cv55>cv55</a>*/ public static final String CV55 = "cv55";
        /**<a href=https://learn.microsoft.com/en-us/typography/opentype/spec/features_ae#cv56>cv56</a>*/ public static final String CV56 = "cv56";
        /**<a href=https://learn.microsoft.com/en-us/typography/opentype/spec/features_ae#cv57>cv57</a>*/ public static final String CV57 = "cv57";
        /**<a href=https://learn.microsoft.com/en-us/typography/opentype/spec/features_ae#cv58>cv58</a>*/ public static final String CV58 = "cv58";
        /**<a href=https://learn.microsoft.com/en-us/typography/opentype/spec/features_ae#cv59>cv59</a>*/ public static final String CV59 = "cv59";
        /**<a href=https://learn.microsoft.com/en-us/typography/opentype/spec/features_ae#cv60>cv60</a>*/ public static final String CV60 = "cv60";
        /**<a href=https://learn.microsoft.com/en-us/typography/opentype/spec/features_ae#cv61>cv61</a>*/ public static final String CV61 = "cv61";
        /**<a href=https://learn.microsoft.com/en-us/typography/opentype/spec/features_ae#cv62>cv62</a>*/ public static final String CV62 = "cv62";
        /**<a href=https://learn.microsoft.com/en-us/typography/opentype/spec/features_ae#cv63>cv63</a>*/ public static final String CV63 = "cv63";
        /**<a href=https://learn.microsoft.com/en-us/typography/opentype/spec/features_ae#cv64>cv64</a>*/ public static final String CV64 = "cv64";
        /**<a href=https://learn.microsoft.com/en-us/typography/opentype/spec/features_ae#cv65>cv65</a>*/ public static final String CV65 = "cv65";
        /**<a href=https://learn.microsoft.com/en-us/typography/opentype/spec/features_ae#cv66>cv66</a>*/ public static final String CV66 = "cv66";
        /**<a href=https://learn.microsoft.com/en-us/typography/opentype/spec/features_ae#cv67>cv67</a>*/ public static final String CV67 = "cv67";
        /**<a href=https://learn.microsoft.com/en-us/typography/opentype/spec/features_ae#cv68>cv68</a>*/ public static final String CV68 = "cv68";
        /**<a href=https://learn.microsoft.com/en-us/typography/opentype/spec/features_ae#cv69>cv69</a>*/ public static final String CV69 = "cv69";
        /**<a href=https://learn.microsoft.com/en-us/typography/opentype/spec/features_ae#cv70>cv70</a>*/ public static final String CV70 = "cv70";
        /**<a href=https://learn.microsoft.com/en-us/typography/opentype/spec/features_ae#cv71>cv71</a>*/ public static final String CV71 = "cv71";
        /**<a href=https://learn.microsoft.com/en-us/typography/opentype/spec/features_ae#cv72>cv72</a>*/ public static final String CV72 = "cv72";
        /**<a href=https://learn.microsoft.com/en-us/typography/opentype/spec/features_ae#cv73>cv73</a>*/ public static final String CV73 = "cv73";
        /**<a href=https://learn.microsoft.com/en-us/typography/opentype/spec/features_ae#cv74>cv74</a>*/ public static final String CV74 = "cv74";
        /**<a href=https://learn.microsoft.com/en-us/typography/opentype/spec/features_ae#cv75>cv75</a>*/ public static final String CV75 = "cv75";
        /**<a href=https://learn.microsoft.com/en-us/typography/opentype/spec/features_ae#cv76>cv76</a>*/ public static final String CV76 = "cv76";
        /**<a href=https://learn.microsoft.com/en-us/typography/opentype/spec/features_ae#cv77>cv77</a>*/ public static final String CV77 = "cv77";
        /**<a href=https://learn.microsoft.com/en-us/typography/opentype/spec/features_ae#cv78>cv78</a>*/ public static final String CV78 = "cv78";
        /**<a href=https://learn.microsoft.com/en-us/typography/opentype/spec/features_ae#cv79>cv79</a>*/ public static final String CV79 = "cv79";
        /**<a href=https://learn.microsoft.com/en-us/typography/opentype/spec/features_ae#cv80>cv80</a>*/ public static final String CV80 = "cv80";
        /**<a href=https://learn.microsoft.com/en-us/typography/opentype/spec/features_ae#cv81>cv81</a>*/ public static final String CV81 = "cv81";
        /**<a href=https://learn.microsoft.com/en-us/typography/opentype/spec/features_ae#cv82>cv82</a>*/ public static final String CV82 = "cv82";
        /**<a href=https://learn.microsoft.com/en-us/typography/opentype/spec/features_ae#cv83>cv83</a>*/ public static final String CV83 = "cv83";
        /**<a href=https://learn.microsoft.com/en-us/typography/opentype/spec/features_ae#cv84>cv84</a>*/ public static final String CV84 = "cv84";
        /**<a href=https://learn.microsoft.com/en-us/typography/opentype/spec/features_ae#cv85>cv85</a>*/ public static final String CV85 = "cv85";
        /**<a href=https://learn.microsoft.com/en-us/typography/opentype/spec/features_ae#cv86>cv86</a>*/ public static final String CV86 = "cv86";
        /**<a href=https://learn.microsoft.com/en-us/typography/opentype/spec/features_ae#cv87>cv87</a>*/ public static final String CV87 = "cv87";
        /**<a href=https://learn.microsoft.com/en-us/typography/opentype/spec/features_ae#cv88>cv88</a>*/ public static final String CV88 = "cv88";
        /**<a href=https://learn.microsoft.com/en-us/typography/opentype/spec/features_ae#cv89>cv89</a>*/ public static final String CV89 = "cv89";
        /**<a href=https://learn.microsoft.com/en-us/typography/opentype/spec/features_ae#cv90>cv90</a>*/ public static final String CV90 = "cv90";
        /**<a href=https://learn.microsoft.com/en-us/typography/opentype/spec/features_ae#cv91>cv91</a>*/ public static final String CV91 = "cv91";
        /**<a href=https://learn.microsoft.com/en-us/typography/opentype/spec/features_ae#cv92>cv92</a>*/ public static final String CV92 = "cv92";
        /**<a href=https://learn.microsoft.com/en-us/typography/opentype/spec/features_ae#cv93>cv93</a>*/ public static final String CV93 = "cv93";
        /**<a href=https://learn.microsoft.com/en-us/typography/opentype/spec/features_ae#cv94>cv94</a>*/ public static final String CV94 = "cv94";
        /**<a href=https://learn.microsoft.com/en-us/typography/opentype/spec/features_ae#cv95>cv95</a>*/ public static final String CV95 = "cv95";
        /**<a href=https://learn.microsoft.com/en-us/typography/opentype/spec/features_ae#cv96>cv96</a>*/ public static final String CV96 = "cv96";
        /**<a href=https://learn.microsoft.com/en-us/typography/opentype/spec/features_ae#cv97>cv97</a>*/ public static final String CV97 = "cv97";
        /**<a href=https://learn.microsoft.com/en-us/typography/opentype/spec/features_ae#cv98>cv98</a>*/ public static final String CV98 = "cv98";
        /**<a href=https://learn.microsoft.com/en-us/typography/opentype/spec/features_ae#cv99>cv99</a>*/ public static final String CV99 = "cv99";
        /**<a href=https://learn.microsoft.com/en-us/typography/opentype/spec/features_ae#c2pc>c2pc</a>*/ public static final String C2PC = "c2pc";
        /**<a href=https://learn.microsoft.com/en-us/typography/opentype/spec/features_ae#c2sc>c2sc</a>*/ public static final String C2SC = "c2sc";
        /**<a href=https://learn.microsoft.com/en-us/typography/opentype/spec/features_ae#dist>dist</a>*/ public static final String DIST = "dist";
        /**<a href=https://learn.microsoft.com/en-us/typography/opentype/spec/features_ae#dlig>dlig</a>*/ public static final String DLIG = "dlig";
        /**<a href=https://learn.microsoft.com/en-us/typography/opentype/spec/features_ae#dnom>dnom</a>*/ public static final String DNOM = "dnom";
        /**<a href=https://learn.microsoft.com/en-us/typography/opentype/spec/features_ae#dtls>dtls</a>*/ public static final String DTLS = "dtls";
        /**<a href=https://learn.microsoft.com/en-us/typography/opentype/spec/features_ae#expt>expt</a>*/ public static final String EXPT = "expt";
        /**<a href=https://learn.microsoft.com/en-us/typography/opentype/spec/features_fj#falt>falt</a>*/ public static final String FALT = "falt";
        /**<a href=https://learn.microsoft.com/en-us/typography/opentype/spec/features_fj#fin2>fin2</a>*/ public static final String FIN2 = "fin2";
        /**<a href=https://learn.microsoft.com/en-us/typography/opentype/spec/features_fj#fin3>fin3</a>*/ public static final String FIN3 = "fin3";
        /**<a href=https://learn.microsoft.com/en-us/typography/opentype/spec/features_fj#fina>fina</a>*/ public static final String FINA = "fina";
        /**<a href=https://learn.microsoft.com/en-us/typography/opentype/spec/features_fj#flac>flac</a>*/ public static final String FLAC = "flac";
        /**<a href=https://learn.microsoft.com/en-us/typography/opentype/spec/features_fj#frac>frac</a>*/ public static final String FRAC = "frac";
        /**<a href=https://learn.microsoft.com/en-us/typography/opentype/spec/features_fj#fwid>fwid</a>*/ public static final String FWID = "fwid";
        /**<a href=https://learn.microsoft.com/en-us/typography/opentype/spec/features_fj#half>half</a>*/ public static final String HALF = "half";
        /**<a href=https://learn.microsoft.com/en-us/typography/opentype/spec/features_fj#haln>haln</a>*/ public static final String HALN = "haln";
        /**<a href=https://learn.microsoft.com/en-us/typography/opentype/spec/features_fj#halt>halt</a>*/ public static final String HALT = "halt";
        /**<a href=https://learn.microsoft.com/en-us/typography/opentype/spec/features_fj#hist>hist</a>*/ public static final String HIST = "hist";
        /**<a href=https://learn.microsoft.com/en-us/typography/opentype/spec/features_fj#hkna>hkna</a>*/ public static final String HKNA = "hkna";
        /**<a href=https://learn.microsoft.com/en-us/typography/opentype/spec/features_fj#hlig>hlig</a>*/ public static final String HLIG = "hlig";
        /**<a href=https://learn.microsoft.com/en-us/typography/opentype/spec/features_fj#hngl>hngl</a>*/ public static final String HNGL = "hngl";
        /**<a href=https://learn.microsoft.com/en-us/typography/opentype/spec/features_fj#hojo>hojo</a>*/ public static final String HOJO = "hojo";
        /**<a href=https://learn.microsoft.com/en-us/typography/opentype/spec/features_fj#hwid>hwid</a>*/ public static final String HWID = "hwid";
        /**<a href=https://learn.microsoft.com/en-us/typography/opentype/spec/features_fj#init>init</a>*/ public static final String INIT = "init";
        /**<a href=https://learn.microsoft.com/en-us/typography/opentype/spec/features_fj#isol>isol</a>*/ public static final String ISOL = "isol";
        /**<a href=https://learn.microsoft.com/en-us/typography/opentype/spec/features_fj#ital>ital</a>*/ public static final String ITAL = "ital";
        /**<a href=https://learn.microsoft.com/en-us/typography/opentype/spec/features_fj#jalt>jalt</a>*/ public static final String JALT = "jalt";
        /**<a href=https://learn.microsoft.com/en-us/typography/opentype/spec/features_fj#jp78>jp78</a>*/ public static final String JP78 = "jp78";
        /**<a href=https://learn.microsoft.com/en-us/typography/opentype/spec/features_fj#jp83>jp83</a>*/ public static final String JP83 = "jp83";
        /**<a href=https://learn.microsoft.com/en-us/typography/opentype/spec/features_fj#jp90>jp90</a>*/ public static final String JP90 = "jp90";
        /**<a href=https://learn.microsoft.com/en-us/typography/opentype/spec/features_fj#jp04>jp04</a>*/ public static final String JP04 = "jp04";
        /**<a href=https://learn.microsoft.com/en-us/typography/opentype/spec/features_ko#lfbd>lfbd</a>*/ public static final String LFBD = "lfbd";
        /**<a href=https://learn.microsoft.com/en-us/typography/opentype/spec/features_ko#ljmo>ljmo</a>*/ public static final String LJMO = "ljmo";
        /**<a href=https://learn.microsoft.com/en-us/typography/opentype/spec/features_ko#lnum>lnum</a>*/ public static final String LNUM = "lnum";
        /**<a href=https://learn.microsoft.com/en-us/typography/opentype/spec/features_ko#locl>locl</a>*/ public static final String LOCL = "locl";
        /**<a href=https://learn.microsoft.com/en-us/typography/opentype/spec/features_ko#ltra>ltra</a>*/ public static final String LTRA = "ltra";
        /**<a href=https://learn.microsoft.com/en-us/typography/opentype/spec/features_ko#ltrm>ltrm</a>*/ public static final String LTRM = "ltrm";
        /**<a href=https://learn.microsoft.com/en-us/typography/opentype/spec/features_ko#mark>mark</a>*/ public static final String MARK = "mark";
        /**<a href=https://learn.microsoft.com/en-us/typography/opentype/spec/features_ko#med2>med2</a>*/ public static final String MED2 = "med2";
        /**<a href=https://learn.microsoft.com/en-us/typography/opentype/spec/features_ko#medi>medi</a>*/ public static final String MEDI = "medi";
        /**<a href=https://learn.microsoft.com/en-us/typography/opentype/spec/features_ko#mgrk>mgrk</a>*/ public static final String MGRK = "mgrk";
        /**<a href=https://learn.microsoft.com/en-us/typography/opentype/spec/features_ko#mkmk>mkmk</a>*/ public static final String MKMK = "mkmk";
        /**<a href=https://learn.microsoft.com/en-us/typography/opentype/spec/features_ko#mset>mset</a>*/ public static final String MSET = "mset";
        /**<a href=https://learn.microsoft.com/en-us/typography/opentype/spec/features_ko#nalt>nalt</a>*/ public static final String NALT = "nalt";
        /**<a href=https://learn.microsoft.com/en-us/typography/opentype/spec/features_ko#nlck>nlck</a>*/ public static final String NLCK = "nlck";
        /**<a href=https://learn.microsoft.com/en-us/typography/opentype/spec/features_ko#nukt>nukt</a>*/ public static final String NUKT = "nukt";
        /**<a href=https://learn.microsoft.com/en-us/typography/opentype/spec/features_ko#numr>numr</a>*/ public static final String NUMR = "numr";
        /**<a href=https://learn.microsoft.com/en-us/typography/opentype/spec/features_ko#onum>onum</a>*/ public static final String ONUM = "onum";
        /**<a href=https://learn.microsoft.com/en-us/typography/opentype/spec/features_ko#opbd>opbd</a>*/ public static final String OPBD = "opbd";
        /**<a href=https://learn.microsoft.com/en-us/typography/opentype/spec/features_ko#ordn>ordn</a>*/ public static final String ORDN = "ordn";
        /**<a href=https://learn.microsoft.com/en-us/typography/opentype/spec/features_ko#ornm>ornm</a>*/ public static final String ORNM = "ornm";
        /**<a href=https://learn.microsoft.com/en-us/typography/opentype/spec/features_pt#palt>palt</a>*/ public static final String PALT = "palt";
        /**<a href=https://learn.microsoft.com/en-us/typography/opentype/spec/features_pt#pcap>pcap</a>*/ public static final String PCAP = "pcap";
        /**<a href=https://learn.microsoft.com/en-us/typography/opentype/spec/features_pt#pkna>pkna</a>*/ public static final String PKNA = "pkna";
        /**<a href=https://learn.microsoft.com/en-us/typography/opentype/spec/features_pt#pnum>pnum</a>*/ public static final String PNUM = "pnum";
        /**<a href=https://learn.microsoft.com/en-us/typography/opentype/spec/features_pt#pref>pref</a>*/ public static final String PREF = "pref";
        /**<a href=https://learn.microsoft.com/en-us/typography/opentype/spec/features_pt#pres>pres</a>*/ public static final String PRES = "pres";
        /**<a href=https://learn.microsoft.com/en-us/typography/opentype/spec/features_pt#pstf>pstf</a>*/ public static final String PSTF = "pstf";
        /**<a href=https://learn.microsoft.com/en-us/typography/opentype/spec/features_pt#psts>psts</a>*/ public static final String PSTS = "psts";
        /**<a href=https://learn.microsoft.com/en-us/typography/opentype/spec/features_pt#pwid>pwid</a>*/ public static final String PWID = "pwid";
        /**<a href=https://learn.microsoft.com/en-us/typography/opentype/spec/features_pt#qwid>qwid</a>*/ public static final String QWID = "qwid";
        /**<a href=https://learn.microsoft.com/en-us/typography/opentype/spec/features_pt#rand>rand</a>*/ public static final String RAND = "rand";
        /**<a href=https://learn.microsoft.com/en-us/typography/opentype/spec/features_pt#rclt>rclt</a>*/ public static final String RCLT = "rclt";
        /**<a href=https://learn.microsoft.com/en-us/typography/opentype/spec/features_pt#rkrf>rkrf</a>*/ public static final String RKRF = "rkrf";
        /**<a href=https://learn.microsoft.com/en-us/typography/opentype/spec/features_pt#rlig>rlig</a>*/ public static final String RLIG = "rlig";
        /**<a href=https://learn.microsoft.com/en-us/typography/opentype/spec/features_pt#rphf>rphf</a>*/ public static final String RPHF = "rphf";
        /**<a href=https://learn.microsoft.com/en-us/typography/opentype/spec/features_pt#rtbd>rtbd</a>*/ public static final String RTBD = "rtbd";
        /**<a href=https://learn.microsoft.com/en-us/typography/opentype/spec/features_pt#rtla>rtla</a>*/ public static final String RTLA = "rtla";
        /**<a href=https://learn.microsoft.com/en-us/typography/opentype/spec/features_pt#rtlm>rtlm</a>*/ public static final String RTLM = "rtlm";
        /**<a href=https://learn.microsoft.com/en-us/typography/opentype/spec/features_pt#ruby>ruby</a>*/ public static final String RUBY = "ruby";
        /**<a href=https://learn.microsoft.com/en-us/typography/opentype/spec/features_pt#rvrn>rvrn</a>*/ public static final String RVRN = "rvrn";
        /**<a href=https://learn.microsoft.com/en-us/typography/opentype/spec/features_pt#salt>salt</a>*/ public static final String SALT = "salt";
        /**<a href=https://learn.microsoft.com/en-us/typography/opentype/spec/features_pt#sinf>sinf</a>*/ public static final String SINF = "sinf";
        /**<a href=https://learn.microsoft.com/en-us/typography/opentype/spec/features_pt#size>size</a>*/ public static final String SIZE = "size";
        /**<a href=https://learn.microsoft.com/en-us/typography/opentype/spec/features_pt#smcp>smcp</a>*/ public static final String SMCP = "smcp";
        /**<a href=https://learn.microsoft.com/en-us/typography/opentype/spec/features_pt#smpl>smpl</a>*/ public static final String SMPL = "smpl";
        /**<a href=https://learn.microsoft.com/en-us/typography/opentype/spec/features_pt#ss01>ss01</a>*/ public static final String SS01 = "ss01";
        /**<a href=https://learn.microsoft.com/en-us/typography/opentype/spec/features_pt#ss02>ss02</a>*/ public static final String SS02 = "ss02";
        /**<a href=https://learn.microsoft.com/en-us/typography/opentype/spec/features_pt#ss03>ss03</a>*/ public static final String SS03 = "ss03";
        /**<a href=https://learn.microsoft.com/en-us/typography/opentype/spec/features_pt#ss04>ss04</a>*/ public static final String SS04 = "ss04";
        /**<a href=https://learn.microsoft.com/en-us/typography/opentype/spec/features_pt#ss05>ss05</a>*/ public static final String SS05 = "ss05";
        /**<a href=https://learn.microsoft.com/en-us/typography/opentype/spec/features_pt#ss06>ss06</a>*/ public static final String SS06 = "ss06";
        /**<a href=https://learn.microsoft.com/en-us/typography/opentype/spec/features_pt#ss07>ss07</a>*/ public static final String SS07 = "ss07";
        /**<a href=https://learn.microsoft.com/en-us/typography/opentype/spec/features_pt#ss08>ss08</a>*/ public static final String SS08 = "ss08";
        /**<a href=https://learn.microsoft.com/en-us/typography/opentype/spec/features_pt#ss09>ss09</a>*/ public static final String SS09 = "ss09";
        /**<a href=https://learn.microsoft.com/en-us/typography/opentype/spec/features_pt#ss10>ss10</a>*/ public static final String SS10 = "ss10";
        /**<a href=https://learn.microsoft.com/en-us/typography/opentype/spec/features_pt#ss11>ss11</a>*/ public static final String SS11 = "ss11";
        /**<a href=https://learn.microsoft.com/en-us/typography/opentype/spec/features_pt#ss12>ss12</a>*/ public static final String SS12 = "ss12";
        /**<a href=https://learn.microsoft.com/en-us/typography/opentype/spec/features_pt#ss13>ss13</a>*/ public static final String SS13 = "ss13";
        /**<a href=https://learn.microsoft.com/en-us/typography/opentype/spec/features_pt#ss14>ss14</a>*/ public static final String SS14 = "ss14";
        /**<a href=https://learn.microsoft.com/en-us/typography/opentype/spec/features_pt#ss15>ss15</a>*/ public static final String SS15 = "ss15";
        /**<a href=https://learn.microsoft.com/en-us/typography/opentype/spec/features_pt#ss16>ss16</a>*/ public static final String SS16 = "ss16";
        /**<a href=https://learn.microsoft.com/en-us/typography/opentype/spec/features_pt#ss17>ss17</a>*/ public static final String SS17 = "ss17";
        /**<a href=https://learn.microsoft.com/en-us/typography/opentype/spec/features_pt#ss18>ss18</a>*/ public static final String SS18 = "ss18";
        /**<a href=https://learn.microsoft.com/en-us/typography/opentype/spec/features_pt#ss19>ss19</a>*/ public static final String SS19 = "ss19";
        /**<a href=https://learn.microsoft.com/en-us/typography/opentype/spec/features_pt#ss20>ss20</a>*/ public static final String SS20 = "ss20";
        /**<a href=https://learn.microsoft.com/en-us/typography/opentype/spec/features_pt#ssty>ssty</a>*/ public static final String SSTY = "ssty";
        /**<a href=https://learn.microsoft.com/en-us/typography/opentype/spec/features_pt#stch>stch</a>*/ public static final String STCH = "stch";
        /**<a href=https://learn.microsoft.com/en-us/typography/opentype/spec/features_pt#subs>subs</a>*/ public static final String SUBS = "subs";
        /**<a href=https://learn.microsoft.com/en-us/typography/opentype/spec/features_pt#sups>sups</a>*/ public static final String SUPS = "sups";
        /**<a href=https://learn.microsoft.com/en-us/typography/opentype/spec/features_pt#swsh>swsh</a>*/ public static final String SWSH = "swsh";
        /**<a href=https://learn.microsoft.com/en-us/typography/opentype/spec/features_pt#titl>titl</a>*/ public static final String TITL = "titl";
        /**<a href=https://learn.microsoft.com/en-us/typography/opentype/spec/features_pt#tjmo>tjmo</a>*/ public static final String TJMO = "tjmo";
        /**<a href=https://learn.microsoft.com/en-us/typography/opentype/spec/features_pt#tnam>tnam</a>*/ public static final String TNAM = "tnam";
        /**<a href=https://learn.microsoft.com/en-us/typography/opentype/spec/features_pt#tnum>tnum</a>*/ public static final String TNUM = "tnum";
        /**<a href=https://learn.microsoft.com/en-us/typography/opentype/spec/features_pt#trad>trad</a>*/ public static final String TRAD = "trad";
        /**<a href=https://learn.microsoft.com/en-us/typography/opentype/spec/features_pt#twid>twid</a>*/ public static final String TWID = "twid";
        /**<a href=https://learn.microsoft.com/en-us/typography/opentype/spec/features_uz#unic>unic</a>*/ public static final String UNIC = "unic";
        /**<a href=https://learn.microsoft.com/en-us/typography/opentype/spec/features_uz#valt>valt</a>*/ public static final String VALT = "valt";
        /**<a href=https://learn.microsoft.com/en-us/typography/opentype/spec/features_uz#vatu>vatu</a>*/ public static final String VATU = "vatu";
        /**<a href=https://learn.microsoft.com/en-us/typography/opentype/spec/features_uz#vchw>vchw</a>*/ public static final String VCHW = "vchw";
        /**<a href=https://learn.microsoft.com/en-us/typography/opentype/spec/features_uz#vert>vert</a>*/ public static final String VERT = "vert";
        /**<a href=https://learn.microsoft.com/en-us/typography/opentype/spec/features_uz#vhal>vhal</a>*/ public static final String VHAL = "vhal";
        /**<a href=https://learn.microsoft.com/en-us/typography/opentype/spec/features_uz#vjmo>vjmo</a>*/ public static final String VJMO = "vjmo";
        /**<a href=https://learn.microsoft.com/en-us/typography/opentype/spec/features_uz#vkna>vkna</a>*/ public static final String VKNA = "vkna";
        /**<a href=https://learn.microsoft.com/en-us/typography/opentype/spec/features_uz#vkrn>vkrn</a>*/ public static final String VKRN = "vkrn";
        /**<a href=https://learn.microsoft.com/en-us/typography/opentype/spec/features_uz#vpal>vpal</a>*/ public static final String VPAL = "vpal";
        /**<a href=https://learn.microsoft.com/en-us/typography/opentype/spec/features_uz#vrt2>vrt2</a>*/ public static final String VRT2 = "vrt2";
        /**<a href=https://learn.microsoft.com/en-us/typography/opentype/spec/features_uz#vrtr>vrtr</a>*/ public static final String VRTR = "vrtr";
        /**<a href=https://learn.microsoft.com/en-us/typography/opentype/spec/features_uz#zero>zero</a>*/ public static final String ZERO = "zero";
    }

    /**
     * This method derives a new {@link java.awt.Font} object with a set of {@linkplain FeatureTag features}.
     * Feature syntax is {@code <name>} or {@code <name>=<int>}, like {@code kern}, or {@code aalt=2}.
     * This method does not preserve features of the original font, they are completely overridden,
     * if you need to append features to the font, use {@link #getEnabledFeatures(Font)}.
     * @param font       basic font
     * @param features   set of OpenType's features
     * @return new font
     */
    Font deriveFontWithFeatures(Font font, String... features);

    /**
     * This method returns an array of features for the given font, set via {@link #deriveFontWithFeatures}.
     * Feature syntax is {@code <name>} or {@code <name>=<int>}, like {@code kern}, or {@code aalt=2}.
     * @param font the font
     * @return an array of features for the font
     */
    String[] getEnabledFeatures(Font font);

    /**
     * This method returns a set of OpenType features supported by the given font
     * @param font the font
     * @return set of supported features
     */
    Set<String> getAvailableFeatures(Font font);

    /**
     * Get subpixel resolution for rendering text with greyscale antialiasing,
     * set with {@code -Djava2d.font.subpixelResolution=NxM}, with integers
     * between 1 and 16 instead of N and M.
     * This only affects text rendered via glyph cache. Value of NxM means
     * that each glyph has rasterized images for N distinct positions horizontally
     * and M positions vertically. This effectively increases quality of glyph
     * spacing in each direction at the cost of N*M times increased memory consumption.
     * @return subpixel resolution (N, M)
     */
    Dimension getSubpixelResolution();
}