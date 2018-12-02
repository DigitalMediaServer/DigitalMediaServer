package net.pms.configuration;


import static org.junit.Assert.assertTrue;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings({ "DLS_DEAD_LOCAL_STORE", "MS_SHOULD_BE_FINAL" })
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class FormatConfigurationRegressionTest0 {

	@Test
	public void testH263() throws Throwable {
		String str0 = FormatConfiguration.H263;

		// Regression assertion (captures the current behavior of the code)
		assertTrue("'" + str0 + "' != '" + "h263"+ "'", str0.equals("h263"));
	}

	@Test
	public void testAVI() throws Throwable {
		String str0 = FormatConfiguration.AVI;

		// Regression assertion (captures the current behavior of the code)
		assertTrue("'" + str0 + "' != '" + "avi"+ "'", str0.equals("avi"));
	}

	@Test
	public void testAU() throws Throwable {
		String str0 = FormatConfiguration.AU;

		// Regression assertion (captures the current behavior of the code)
		assertTrue("'" + str0 + "' != '" + "au"+ "'", str0.equals("au"));
	}

	@Test
	public void testMPEGPS() throws Throwable {
		String str0 = FormatConfiguration.MPEGPS;

		// Regression assertion (captures the current behavior of the code)
		assertTrue("'" + str0 + "' != '" + "mpegps"+ "'", str0.equals("mpegps"));
	}

	@Test
	public void testWMALOSSLESS() throws Throwable {
		String str0 = FormatConfiguration.WMALOSSLESS;

		// Regression assertion (captures the current behavior of the code)
		assertTrue("'" + str0 + "' != '" + "wmalossless"+ "'", str0.equals("wmalossless"));
	}

	@Test
	public void testFLV() throws Throwable {
		String str0 = FormatConfiguration.FLV;

		// Regression assertion (captures the current behavior of the code)
		assertTrue("'" + str0 + "' != '" + "flv"+ "'", str0.equals("flv"));
	}

	@Test
	public void testOPUS() throws Throwable {
		String str0 = FormatConfiguration.OPUS;

		// Regression assertion (captures the current behavior of the code)
		assertTrue("'" + str0 + "' != '" + "opus"+ "'", str0.equals("opus"));
	}

	@Test
	public void testCOOK() throws Throwable {
		String str0 = FormatConfiguration.COOK;

		// Regression assertion (captures the current behavior of the code)
		assertTrue("'" + str0 + "' != '" + "cook"+ "'", str0.equals("cook"));
	}

	@Test
	public void testADTS() throws Throwable {
		String str0 = FormatConfiguration.ADTS;

		// Regression assertion (captures the current behavior of the code)
		assertTrue("'" + str0 + "' != '" + "adts"+ "'", str0.equals("adts"));
	}

	@Test
	public void testRA() throws Throwable {
		String str0 = FormatConfiguration.RA;

		// Regression assertion (captures the current behavior of the code)
		assertTrue("'" + str0 + "' != '" + "ra"+ "'", str0.equals("ra"));
	}

	@Test
	public void testMKV() throws Throwable {
		String str0 = FormatConfiguration.MKV;

		// Regression assertion (captures the current behavior of the code)
		assertTrue("'" + str0 + "' != '" + "mkv"+ "'", str0.equals("mkv"));
	}

	@Test
	public void testVC1() throws Throwable {
		String str0 = FormatConfiguration.VC1;

		// Regression assertion (captures the current behavior of the code)
		assertTrue("'" + str0 + "' != '" + "vc1"+ "'", str0.equals("vc1"));
	}

	@Test
	public void testMP2() throws Throwable {
		String str0 = FormatConfiguration.MP2;

		// Regression assertion (captures the current behavior of the code)
		assertTrue("'" + str0 + "' != '" + "mp2"+ "'", str0.equals("mp2"));
	}

	@Test
	public void testLPCM() throws Throwable {
		String str0 = FormatConfiguration.LPCM;

		// Regression assertion (captures the current behavior of the code)
		assertTrue("'" + str0 + "' != '" + "lpcm"+ "'", str0.equals("lpcm"));
	}

	@Test
	public void testMPEG2() throws Throwable {
		String str0 = FormatConfiguration.MPEG2;

		// Regression assertion (captures the current behavior of the code)
		assertTrue("'" + str0 + "' != '" + "mpeg2"+ "'", str0.equals("mpeg2"));
	}

	@Test
	public void testM4A() throws Throwable {
		String str0 = FormatConfiguration.M4A;

		// Regression assertion (captures the current behavior of the code)
		assertTrue("'" + str0 + "' != '" + "m4a"+ "'", str0.equals("m4a"));
	}

	@Test
	public void testDIVX() throws Throwable {
		String str0 = FormatConfiguration.DIVX;

		// Regression assertion (captures the current behavior of the code)
		assertTrue("'" + str0 + "' != '" + "divx"+ "'", str0.equals("divx"));
	}

	@Test
	public void testTHREEGPP2() throws Throwable {
		String str0 = FormatConfiguration.THREEGPP2;

		// Regression assertion (captures the current behavior of the code)
		assertTrue("'" + str0 + "' != '" + "3g2"+ "'", str0.equals("3g2"));
	}

	@Test
	public void testOGG() throws Throwable {
		String str0 = FormatConfiguration.OGG;

		// Regression assertion (captures the current behavior of the code)
		assertTrue("'" + str0 + "' != '" + "ogg"+ "'", str0.equals("ogg"));
	}

	@Test
	public void testAC3() throws Throwable {
		String str0 = FormatConfiguration.AC3;

		// Regression assertion (captures the current behavior of the code)
		assertTrue("'" + str0 + "' != '" + "ac3"+ "'", str0.equals("ac3"));
	}

	@Test
	public void testATMOS() throws Throwable {
		String str0 = FormatConfiguration.ATMOS;

		// Regression assertion (captures the current behavior of the code)
		assertTrue("'" + str0 + "' != '" + "atmos"+ "'", str0.equals("atmos"));
	}

	@Test
	public void testWMAPRO() throws Throwable {
		String str0 = FormatConfiguration.WMAPRO;

		// Regression assertion (captures the current behavior of the code)
		assertTrue("'" + str0 + "' != '" + "wmapro"+ "'", str0.equals("wmapro"));
	}

	@Test
	public void testGIF() throws Throwable {
		String str0 = FormatConfiguration.GIF;

		// Regression assertion (captures the current behavior of the code)
		assertTrue("'" + str0 + "' != '" + "gif"+ "'", str0.equals("gif"));
	}

	@Test
	public void testMP4() throws Throwable {
		String str0 = FormatConfiguration.MP4;

		// Regression assertion (captures the current behavior of the code)
		assertTrue("'" + str0 + "' != '" + "mp4"+ "'", str0.equals("mp4"));
	}

	@Test
	public void testH264() throws Throwable {
		String str0 = FormatConfiguration.H264;

		// Regression assertion (captures the current behavior of the code)
		assertTrue("'" + str0 + "' != '" + "h264"+ "'", str0.equals("h264"));
	}

	@Test
	public void testATRAC() throws Throwable {
		String str0 = FormatConfiguration.ATRAC;

		// Regression assertion (captures the current behavior of the code)
		assertTrue("'" + str0 + "' != '" + "atrac"+ "'", str0.equals("atrac"));
	}

	@Test
	public void testHEAAC() throws Throwable {
		String str0 = FormatConfiguration.HE_AAC;

		// Regression assertion (captures the current behavior of the code)
		assertTrue("'" + str0 + "' != '" + "he-aac"+ "'", str0.equals("he-aac"));
	}

	@Test
	public void testWEBM() throws Throwable {
		String str0 = FormatConfiguration.WEBM;

		// Regression assertion (captures the current behavior of the code)
		assertTrue("'" + str0 + "' != '" + "webm"+ "'", str0.equals("webm"));
	}

	@Test
	public void testTIFF() throws Throwable {
		String str0 = FormatConfiguration.TIFF;

		// Regression assertion (captures the current behavior of the code)
		assertTrue("'" + str0 + "' != '" + "tiff"+ "'", str0.equals("tiff"));
	}

	@Test
	public void testWAV() throws Throwable {
		String str0 = FormatConfiguration.WAV;

		// Regression assertion (captures the current behavior of the code)
		assertTrue("'" + str0 + "' != '" + "wav"+ "'", str0.equals("wav"));
	}

	@Test
	public void testJPG() throws Throwable {
		String str0 = FormatConfiguration.JPG;

		// Regression assertion (captures the current behavior of the code)
		assertTrue("'" + str0 + "' != '" + "jpg"+ "'", str0.equals("jpg"));
	}

	@Test
	public void testVP9() throws Throwable {
		String str0 = FormatConfiguration.VP9;

		// Regression assertion (captures the current behavior of the code)
		assertTrue("'" + str0 + "' != '" + "vp9"+ "'", str0.equals("vp9"));
	}

	@Test
	public void test33() throws Throwable {
		String str0 = FormatConfiguration.MI_GOP;

		// Regression assertion (captures the current behavior of the code)
		assertTrue("'" + str0 + "' != '" + "gop"+ "'", str0.equals("gop"));
	}

	@Test
	public void testVP8() throws Throwable {
		String str0 = FormatConfiguration.VP8;

		// Regression assertion (captures the current behavior of the code)
		assertTrue("'" + str0 + "' != '" + "vp8"+ "'", str0.equals("vp8"));
	}

	@Test
	public void testTHREEGA() throws Throwable {
		String str0 = FormatConfiguration.THREEGA;

		// Regression assertion (captures the current behavior of the code)
		assertTrue("'" + str0 + "' != '" + "3ga"+ "'", str0.equals("3ga"));
	}

	@Test
	public void testMONKEYS_AUDIO() throws Throwable {
		String str0 = FormatConfiguration.MONKEYS_AUDIO;

		// Regression assertion (captures the current behavior of the code)
		assertTrue("'" + str0 + "' != '" + "ape"+ "'", str0.equals("ape"));
	}

	@Test
	public void testRALF() throws Throwable {
		String str0 = FormatConfiguration.RALF;

		// Regression assertion (captures the current behavior of the code)
		assertTrue("'" + str0 + "' != '" + "ralf"+ "'", str0.equals("ralf"));
	}

	@Test
	public void testMKA() throws Throwable {
		String str0 = FormatConfiguration.MKA;

		// Regression assertion (captures the current behavior of the code)
		assertTrue("'" + str0 + "' != '" + "mka"+ "'", str0.equals("mka"));
	}

	@Test
	public void testDSF() throws Throwable {
		String str0 = FormatConfiguration.DSF;

		// Regression assertion (captures the current behavior of the code)
		assertTrue("'" + str0 + "' != '" + "dsf"+ "'", str0.equals("dsf"));
	}

	@Test
	public void testCINEPAK() throws Throwable {
		String str0 = FormatConfiguration.CINEPAK;

		// Regression assertion (captures the current behavior of the code)
		assertTrue("'" + str0 + "' != '" + "cvid"+ "'", str0.equals("cvid"));
	}

	@Test
	public void testMIMETYPE_AUTO() throws Throwable {
		String str0 = FormatConfiguration.MIMETYPE_AUTO;

		// Regression assertion (captures the current behavior of the code)
		assertTrue("'" + str0 + "' != '" + "MIMETYPE_AUTO"+ "'", str0.equals("MIMETYPE_AUTO"));
	}

	@Test
	public void testVP7() throws Throwable {
		String str0 = FormatConfiguration.VP7;

		// Regression assertion (captures the current behavior of the code)
		assertTrue("'" + str0 + "' != '" + "vp7"+ "'", str0.equals("vp7"));
	}

	@Test
	public void testTHREEGPP() throws Throwable {
		String str0 = FormatConfiguration.THREEGPP;

		// Regression assertion (captures the current behavior of the code)
		assertTrue("'" + str0 + "' != '" + "3gp"+ "'", str0.equals("3gp"));
	}

	@Test
	public void testMJPEG() throws Throwable {
		String str0 = FormatConfiguration.MJPEG;

		// Regression assertion (captures the current behavior of the code)
		assertTrue("'" + str0 + "' != '" + "mjpeg"+ "'", str0.equals("mjpeg"));
	}

	@Test
	public void testBMP() throws Throwable {
		String str0 = FormatConfiguration.BMP;

		// Regression assertion (captures the current behavior of the code)
		assertTrue("'" + str0 + "' != '" + "bmp"+ "'", str0.equals("bmp"));
	}

	@Test
	public void testTTA() throws Throwable {
		String str0 = FormatConfiguration.TTA;

		// Regression assertion (captures the current behavior of the code)
		assertTrue("'" + str0 + "' != '" + "tta"+ "'", str0.equals("tta"));
	}

	@Test
	public void testAACLC() throws Throwable {
		String str0 = FormatConfiguration.AAC_LC;

		// Regression assertion (captures the current behavior of the code)
		assertTrue("'" + str0 + "' != '" + "aac-lc"+ "'", str0.equals("aac-lc"));
	}

	@Test
	public void testPNG() throws Throwable {
		String str0 = FormatConfiguration.PNG;

		// Regression assertion (captures the current behavior of the code)
		assertTrue("'" + str0 + "' != '" + "png"+ "'", str0.equals("png"));
	}

	@Test
	public void testWMV() throws Throwable {
		String str0 = FormatConfiguration.WMV;

		// Regression assertion (captures the current behavior of the code)
		assertTrue("'" + str0 + "' != '" + "wmv"+ "'", str0.equals("wmv"));
	}

	@Test
	public void testMPEGTS() throws Throwable {
		String str0 = FormatConfiguration.MPEGTS;

		// Regression assertion (captures the current behavior of the code)
		assertTrue("'" + str0 + "' != '" + "mpegts"+ "'", str0.equals("mpegts"));
	}

	@Test
	public void testDTS() throws Throwable {
		String str0 = FormatConfiguration.DTS;

		// Regression assertion (captures the current behavior of the code)
		assertTrue("'" + str0 + "' != '" + "dts"+ "'", str0.equals("dts"));
	}

	@Test
	public void testVP6() throws Throwable {
		String str0 = FormatConfiguration.VP6;

		// Regression assertion (captures the current behavior of the code)
		assertTrue("'" + str0 + "' != '" + "vp6"+ "'", str0.equals("vp6"));
	}

	@Test
	public void testQDESIGN() throws Throwable {
		String str0 = FormatConfiguration.QDESIGN;

		// Regression assertion (captures the current behavior of the code)
		assertTrue("'" + str0 + "' != '" + "qdmc"+ "'", str0.equals("qdmc"));
	}

	@Test
	public void testMPA() throws Throwable {
		String str0 = FormatConfiguration.MPA;

		// Regression assertion (captures the current behavior of the code)
		assertTrue("'" + str0 + "' != '" + "mpa"+ "'", str0.equals("mpa"));
	}

	@Test
	public void testTRUEHD() throws Throwable {
		String str0 = FormatConfiguration.TRUEHD;

		// Regression assertion (captures the current behavior of the code)
		assertTrue("'" + str0 + "' != '" + "truehd"+ "'", str0.equals("truehd"));
	}

	@Test
	public void testALAC() throws Throwable {
		String str0 = FormatConfiguration.ALAC;

		// Regression assertion (captures the current behavior of the code)
		assertTrue("'" + str0 + "' != '" + "alac"+ "'", str0.equals("alac"));
	}

	@Test
	public void testMPEG1() throws Throwable {
		String str0 = FormatConfiguration.MPEG1;

		// Regression assertion (captures the current behavior of the code)
		assertTrue("'" + str0 + "' != '" + "mpeg1"+ "'", str0.equals("mpeg1"));
	}

	@Test
	public void testMPC() throws Throwable {
		String str0 = FormatConfiguration.MPC;

		// Regression assertion (captures the current behavior of the code)
		assertTrue("'" + str0 + "' != '" + "mpc"+ "'", str0.equals("mpc"));
	}

	@Test
	public void test59() throws Throwable {
		String str0 = FormatConfiguration.MI_QPEL;

		// Regression assertion (captures the current behavior of the code)
		assertTrue("'" + str0 + "' != '" + "qpel"+ "'", str0.equals("qpel"));
	}

	@Test
	public void testEAC3() throws Throwable {
		String str0 = FormatConfiguration.EAC3;

		// Regression assertion (captures the current behavior of the code)
		assertTrue("'" + str0 + "' != '" + "eac3"+ "'", str0.equals("eac3"));
	}

	@Test
	public void testVORBIS() throws Throwable {
		String str0 = FormatConfiguration.VORBIS;

		// Regression assertion (captures the current behavior of the code)
		assertTrue("'" + str0 + "' != '" + "vorbis"+ "'", str0.equals("vorbis"));
	}

	@Test
	public void testUnd() throws Throwable {
		String str0 = FormatConfiguration.und;

		// Regression assertion (captures the current behavior of the code)
		assertTrue("'" + str0 + "' != '" + "und"+ "'", str0.equals("und"));
	}

	@Test
	public void testH265() throws Throwable {
		String str0 = FormatConfiguration.H265;

		// Regression assertion (captures the current behavior of the code)
		assertTrue("'" + str0 + "' != '" + "h265"+ "'", str0.equals("h265"));
	}

	@Test
	public void testWMA() throws Throwable {
		String str0 = FormatConfiguration.WMA;

		// Regression assertion (captures the current behavior of the code)
		assertTrue("'" + str0 + "' != '" + "wma"+ "'", str0.equals("wma"));
	}

	@Test
	public void testTHEORA() throws Throwable {
		String str0 = FormatConfiguration.THEORA;

		// Regression assertion (captures the current behavior of the code)
		assertTrue("'" + str0 + "' != '" + "theora"+ "'", str0.equals("theora"));
	}

	@Test
	public void test66() throws Throwable {
		String str0 = FormatConfiguration.MI_GMC;

		// Regression assertion (captures the current behavior of the code)
		assertTrue("'" + str0 + "' != '" + "gmc"+ "'", str0.equals("gmc"));
	}

	@Test
	public void testSHORTEN() throws Throwable {
		String str0 = FormatConfiguration.SHORTEN;

		// Regression assertion (captures the current behavior of the code)
		assertTrue("'" + str0 + "' != '" + "shn"+ "'", str0.equals("shn"));
	}

	@Test
	public void testADPCM() throws Throwable {
		String str0 = FormatConfiguration.ADPCM;

		// Regression assertion (captures the current behavior of the code)
		assertTrue("'" + str0 + "' != '" + "adpcm"+ "'", str0.equals("adpcm"));
	}

	@Test
	public void testWMAVOICE() throws Throwable {
		String str0 = FormatConfiguration.WMAVOICE;

		// Regression assertion (captures the current behavior of the code)
		assertTrue("'" + str0 + "' != '" + "wmavoice"+ "'", str0.equals("wmavoice"));
	}

	@Test
	public void testDV() throws Throwable {
		String str0 = FormatConfiguration.DV;

		// Regression assertion (captures the current behavior of the code)
		assertTrue("'" + str0 + "' != '" + "dv"+ "'", str0.equals("dv"));
	}

	@Test
	public void testFLAC() throws Throwable {
		String str0 = FormatConfiguration.FLAC;

		// Regression assertion (captures the current behavior of the code)
		assertTrue("'" + str0 + "' != '" + "flac"+ "'", str0.equals("flac"));
	}

	@Test
	public void testRM() throws Throwable {
		String str0 = FormatConfiguration.RM;

		// Regression assertion (captures the current behavior of the code)
		assertTrue("'" + str0 + "' != '" + "rm"+ "'", str0.equals("rm"));
	}

	@Test
	public void testWAVPACK() throws Throwable {
		String str0 = FormatConfiguration.WAVPACK;

		// Regression assertion (captures the current behavior of the code)
		assertTrue("'" + str0 + "' != '" + "wavpack"+ "'", str0.equals("wavpack"));
	}

	@Test
	public void testMOV() throws Throwable {
		String str0 = FormatConfiguration.MOV;

		// Regression assertion (captures the current behavior of the code)
		assertTrue("'" + str0 + "' != '" + "mov"+ "'", str0.equals("mov"));
	}

	@Test
	public void testSOR() throws Throwable {
		String str0 = FormatConfiguration.SORENSON;

		// Regression assertion (captures the current behavior of the code)
		assertTrue("'" + str0 + "' != '" + "sor"+ "'", str0.equals("sor"));
	}

	@Test
	public void testMLP() throws Throwable {
		String str0 = FormatConfiguration.MLP;

		// Regression assertion (captures the current behavior of the code)
		assertTrue("'" + str0 + "' != '" + "mlp"+ "'", str0.equals("mlp"));
	}

	@Test
	public void testAMR() throws Throwable {
		String str0 = FormatConfiguration.AMR;

		// Regression assertion (captures the current behavior of the code)
		assertTrue("'" + str0 + "' != '" + "amr"+ "'", str0.equals("amr"));
	}

	@Test
	public void testMP3() throws Throwable {
		String str0 = FormatConfiguration.MP3;

		// Regression assertion (captures the current behavior of the code)
		assertTrue("'" + str0 + "' != '" + "mp3"+ "'", str0.equals("mp3"));
	}

	@Test
	public void testDTSHD() throws Throwable {
		String str0 = FormatConfiguration.DTSHD;

		// Regression assertion (captures the current behavior of the code)
		assertTrue("'" + str0 + "' != '" + "dtshd"+ "'", str0.equals("dtshd"));
	}

	@Test
	public void testAIFF() throws Throwable {
		String str0 = FormatConfiguration.AIFF;

		// Regression assertion (captures the current behavior of the code)
		assertTrue("'" + str0 + "' != '" + "aiff"+ "'", str0.equals("aiff"));
	}

	@Test
	public void testAIFC() throws Throwable {
		String str0 = FormatConfiguration.AIFC;

		// Regression assertion (captures the current behavior of the code)
		assertTrue("'" + str0 + "' != '" + "aifc"+ "'", str0.equals("aifc"));
	}

	@Test
	public void testALS() throws Throwable {
		String str0 = FormatConfiguration.ALS;

		// Regression assertion (captures the current behavior of the code)
		assertTrue("'" + str0 + "' != '" + "als"+ "'", str0.equals("als"));
	}

	@Test
	public void testOGA() throws Throwable {
		String str0 = FormatConfiguration.OGA;

		// Regression assertion (captures the current behavior of the code)
		assertTrue("'" + str0 + "' != '" + "oga"+ "'", str0.equals("oga"));
	}

	@Test
	public void testRealAudio_14_4() throws Throwable {
		String str0 = FormatConfiguration.REALAUDIO_14_4;

		// Regression assertion (captures the current behavior of the code)
		assertTrue("'" + str0 + "' != '" + "ra14.4"+ "'", str0.equals("ra14.4"));
	}

	@Test
	public void testRealAudio_28_8() throws Throwable {
		String str0 = FormatConfiguration.REALAUDIO_28_8;

		// Regression assertion (captures the current behavior of the code)
		assertTrue("'" + str0 + "' != '" + "ra28.8"+ "'", str0.equals("ra28.8"));
	}

	@Test
	public void testSipro() throws Throwable {
		String str0 = FormatConfiguration.SIPRO;

		// Regression assertion (captures the current behavior of the code)
		assertTrue("'" + str0 + "' != '" + "sipro"+ "'", str0.equals("sipro"));
	}

	@Test
	public void testACELP() throws Throwable {
		String str0 = FormatConfiguration.ACELP;

		// Regression assertion (captures the current behavior of the code)
		assertTrue("'" + str0 + "' != '" + "acelp"+ "'", str0.equals("acelp"));
	}

		@Test
	public void testG729() throws Throwable {
		String str0 = FormatConfiguration.G729;

		// Regression assertion (captures the current behavior of the code)
		assertTrue("'" + str0 + "' != '" + "g729"+ "'", str0.equals("g729"));
	}

	@Test
	public void testWMA10() throws Throwable {
		String str0 = FormatConfiguration.WMA10;

		// Regression assertion (captures the current behavior of the code)
		assertTrue("'" + str0 + "' != '" + "wma10"+ "'", str0.equals("wma10"));
	}
	@Test
	public void testDFF() throws Throwable {
		String str0 = FormatConfiguration.DFF;

		// Regression assertion (captures the current behavior of the code)
		assertTrue("'" + str0 + "' != '" + "dff"+ "'", str0.equals("dff"));
	}

	@Test
	public void testH261() throws Throwable {
		String str0 = FormatConfiguration.H261;

		// Regression assertion (captures the current behavior of the code)
		assertTrue("'" + str0 + "' != '" + "h261"+ "'", str0.equals("h261"));
	}

	@Test
	public void testINDEO() throws Throwable {
		String str0 = FormatConfiguration.INDEO;

		// Regression assertion (captures the current behavior of the code)
		assertTrue("'" + str0 + "' != '" + "indeo"+ "'", str0.equals("indeo"));
	}

	@Test
	public void testRGB() throws Throwable {
		String str0 = FormatConfiguration.RGB;

		// Regression assertion (captures the current behavior of the code)
		assertTrue("'" + str0 + "' != '" + "rgb"+ "'", str0.equals("rgb"));
	}

	@Test
	public void testYUV() throws Throwable {
		String str0 = FormatConfiguration.YUV;

		// Regression assertion (captures the current behavior of the code)
		assertTrue("'" + str0 + "' != '" + "yuv"+ "'", str0.equals("yuv"));
	}

	@Test
	public void testNELLYMOSER() throws Throwable {
		String str0 = FormatConfiguration.NELLYMOSER;

		// Regression assertion (captures the current behavior of the code)
		assertTrue("'" + str0 + "' != '" + "nellymoser"+ "'", str0.equals("nellymoser"));
	}

	@Test
	public void testAAC_LTP() throws Throwable {
		java.lang.String str0 = net.pms.configuration.FormatConfiguration.AAC_LTP;

		// Regression assertion (captures the current behavior of the code)
		org.junit.Assert.assertTrue("'" + str0 + "' != '" + "aac-ltp"+ "'", str0.equals("aac-ltp"));
	}

	@Test
	public void testRLE() throws Throwable {
		String str0 = FormatConfiguration.RLE;

		// Regression assertion (captures the current behavior of the code)
		assertTrue("'" + str0 + "' != '" + "rle"+ "'", str0.equals("rle"));
	}

	@Test
	public void testAAC_MAIN() throws Throwable {
		java.lang.String str0 = net.pms.configuration.FormatConfiguration.AAC_MAIN;

		// Regression assertion (captures the current behavior of the code)
		org.junit.Assert.assertTrue("'" + str0 + "' != '" + "aac-main"+ "'", str0.equals("aac-main"));
	}

	@Test
	public void testAAC_SSR() throws Throwable {
		java.lang.String str0 = net.pms.configuration.FormatConfiguration.AAC_SSR;

		// Regression assertion (captures the current behavior of the code)
		org.junit.Assert.assertTrue("'" + str0 + "' != '" + "aac-ssr"+ "'", str0.equals("aac-ssr"));
	}

	@Test
	public void testCAF() throws Throwable {
		String str0 = FormatConfiguration.CAF;

		// Regression assertion (captures the current behavior of the code)
		assertTrue("'" + str0 + "' != '" + "caf"+ "'", str0.equals("caf"));
	}

	@Test
	public void testMACE3() throws Throwable {
		String str0 = FormatConfiguration.MACE3;

		// Regression assertion (captures the current behavior of the code)
		assertTrue("'" + str0 + "' != '" + "mace3"+ "'", str0.equals("mace3"));
	}

	@Test
	public void testMACE6() throws Throwable {
		String str0 = FormatConfiguration.MACE6;

		// Regression assertion (captures the current behavior of the code)
		assertTrue("'" + str0 + "' != '" + "mace6"+ "'", str0.equals("mace6"));
	}

	@Test
	public void testTGA() throws Throwable {
		String str0 = FormatConfiguration.TGA;

		// Regression assertion (captures the current behavior of the code)
		assertTrue("'" + str0 + "' != '" + "tga"+ "'", str0.equals("tga"));
	}

	@Test
	public void testFFV1() throws Throwable {
		String str0 = FormatConfiguration.FFV1;

		// Regression assertion (captures the current behavior of the code)
		assertTrue("'" + str0 + "' != '" + "ffv1"+ "'", str0.equals("ffv1"));
	}

	@Test
	public void testCELP() throws Throwable {
		String str0 = FormatConfiguration.CELP;

		// Regression assertion (captures the current behavior of the code)
		assertTrue("'" + str0 + "' != '" + "celp"+ "'", str0.equals("celp"));
	}

	@Test
	public void testQCELP() throws Throwable {
		String str0 = FormatConfiguration.QCELP;

		// Regression assertion (captures the current behavior of the code)
		assertTrue("'" + str0 + "' != '" + "qcelp"+ "'", str0.equals("qcelp"));
	}

	@Test
	public void testMPEG4ASP() throws Throwable {
		String str0 = FormatConfiguration.MPEG4ASP;

		// Regression assertion (captures the current behavior of the code)
		assertTrue("'" + str0 + "' != '" + "mpeg4asp"+ "'", str0.equals("mpeg4asp"));
	}

	@Test
	public void testMPEG4SP() throws Throwable {
		String str0 = net.pms.configuration.FormatConfiguration.MPEG4SP;

		// Regression assertion (captures the current behavior of the code)
		assertTrue("'" + str0 + "' != '" + "mpeg4sp"+ "'", str0.equals("mpeg4sp"));
	}

	@Test
	public void testDOLBYE() throws Throwable {
		String str0 = net.pms.configuration.FormatConfiguration.DOLBYE;

		// Regression assertion (captures the current behavior of the code)
		assertTrue("'" + str0 + "' != '" + "dolbye"+ "'", str0.equals("dolbye"));
	}

	@Test
	public void testAV1() throws Throwable {
		String str0 = net.pms.configuration.FormatConfiguration.AV1;

		// Regression assertion (captures the current behavior of the code)
		assertTrue("'" + str0 + "' != '" + "av1"+ "'", str0.equals("av1"));
	}

	@Test
	public void testMXF() throws Throwable {
		String str0 = net.pms.configuration.FormatConfiguration.MXF;

		// Regression assertion (captures the current behavior of the code)
		assertTrue("'" + str0 + "' != '" + "mxf"+ "'", str0.equals("mxf"));
	}

	@Test
	public void testVC3() throws Throwable {
		String str0 = net.pms.configuration.FormatConfiguration.VC3;

		// Regression assertion (captures the current behavior of the code)
		assertTrue("'" + str0 + "' != '" + "vc3"+ "'", str0.equals("vc3"));
	}

	@Test
	public void testPRORES() throws Throwable {
		String str0 = net.pms.configuration.FormatConfiguration.PRORES;

		// Regression assertion (captures the current behavior of the code)
		assertTrue("'" + str0 + "' != '" + "prores"+ "'", str0.equals("prores"));
	}

	@Test
	public void testMJP2() throws Throwable {
		String str0 = net.pms.configuration.FormatConfiguration.MJP2;

		// Regression assertion (captures the current behavior of the code)
		assertTrue("'" + str0 + "' != '" + "mjp2"+ "'", str0.equals("mjp2"));
	}

	@Test
	public void testSUDS() throws Throwable {
		String str0 = net.pms.configuration.FormatConfiguration.SUDS;

		// Regression assertion (captures the current behavior of the code)
		assertTrue("'" + str0 + "' != '" + "suds"+ "'", str0.equals("suds"));
	}
}
