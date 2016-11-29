package boa.dsi.dsource.fars;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.google.protobuf.CodedInputStream;
import com.google.protobuf.GeneratedMessage;

import boa.dsi.dsource.AbstractSource;
import boa.dsi.dsource.dbf.DBF;
import boa.types.Transport.Crash;
import boa.types.Transport.Person;
import boa.types.Transport.Vehicle;
import boa.types.Transport.STATE;

public class FARS extends AbstractSource {

	private DBF reader;

	public FARS(ArrayList<String> source) {
		super(source);
		if (source.size() < 3) {
			throw new IllegalArgumentException("FARS Data set accepts atleast Accident, Vehicle and Person Data");
		}
		// reader = new DBF(source.get(0));
	}

	@Override
	public GeneratedMessage parseFrom(CodedInputStream stream) throws IOException {
		return Crash.parseFrom(stream);
	}

	@Override
	public boolean isReadable(String source) {
		return reader.isReadable(source);
	}

	@Override
	public List<GeneratedMessage> getData() {
		return this.buildData();
	}

	@Override
	public String getParserClassName() {
		throw new UnsupportedOperationException();
	}

	private List<GeneratedMessage> buildData() {
		List<String> accidentFields = new ArrayList<String>();
		this.reader = new DBF(getFilePath("accident"));
		List<Object[]> accidentData = this.reader.getData(accidentFields);

		List<String> personFields = new ArrayList<String>();
		this.reader = new DBF(getFilePath("person"));
		List<Object[]> persontData = this.reader.getData(personFields);

		List<String> vehicleFields = new ArrayList<String>();
		this.reader = new DBF(getFilePath("vehicle"));
		List<Object[]> vehicleData = this.reader.getData(vehicleFields);

		List<GeneratedMessage> data = new ArrayList<GeneratedMessage>();

		System.out.println("total crash: " + accidentData.size());
		int count = 1;
		for (Object[] accident : accidentData) {
			Crash.Builder cb = Crash.newBuilder();
			String stateCaseId = accident[accidentFields.indexOf("ST_CASE")].toString();
			int caseId = (int)Double.parseDouble(stateCaseId.substring(0, stateCaseId.indexOf('.')));
			cb.setSTCASE(caseId);
			cb.setState(getState((int)Double.parseDouble(accident[accidentFields.indexOf("STATE")].toString())));
			cb.setSTCASE((int)Double.parseDouble(accident[accidentFields.indexOf("ST_CASE")].toString()));
			cb.setVETOTAL((int)Double.parseDouble(accident[accidentFields.indexOf("VE_TOTAL")].toString()));
			cb.setVEFORMS((int)Double.parseDouble(accident[accidentFields.indexOf("VE_FORMS")].toString()));
			cb.setPVHINVL((int)Double.parseDouble(accident[accidentFields.indexOf("PVH_INVL")].toString()));
			cb.setPEDS((int)Double.parseDouble(accident[accidentFields.indexOf("PEDS")].toString()));
			cb.setPERNOTMVIT((int)Double.parseDouble(accident[accidentFields.indexOf("PERNOTMVIT")].toString()));
			cb.setPERMVIT((int)Double.parseDouble(accident[accidentFields.indexOf("PERMVIT")].toString()));
			cb.setPersonCount((int)Double.parseDouble(accident[accidentFields.indexOf("PERSONS")].toString()));
			cb.setCOUNTY((int)Double.parseDouble(accident[accidentFields.indexOf("COUNTY")].toString()));
			cb.setCITY((int)Double.parseDouble(accident[accidentFields.indexOf("CITY")].toString()));
			cb.setDAY((int)Double.parseDouble(accident[accidentFields.indexOf("DAY")].toString()));
			cb.setMONTH((int)Double.parseDouble(accident[accidentFields.indexOf("MONTH")].toString()));
			cb.setYEAR((int)Double.parseDouble(accident[accidentFields.indexOf("YEAR")].toString()));
			cb.setDAYWEEK((int)Double.parseDouble(accident[accidentFields.indexOf("DAY_WEEK")].toString()));
			cb.setHOUR((int)Double.parseDouble(accident[accidentFields.indexOf("HOUR")].toString()));
			cb.setMINUTE((int)Double.parseDouble(accident[accidentFields.indexOf("MINUTE")].toString()));
			cb.setNHS((int)Double.parseDouble(accident[accidentFields.indexOf("NHS")].toString()));
			cb.setRURURB((int)Double.parseDouble(accident[accidentFields.indexOf("RUR_URB")].toString()));
			cb.setFUNCSYS((int)Double.parseDouble(accident[accidentFields.indexOf("FUNC_SYS")].toString()));
			cb.setRDOWNER((int)Double.parseDouble(accident[accidentFields.indexOf("RD_OWNER")].toString()));
			cb.setROUTE((int)Double.parseDouble(accident[accidentFields.indexOf("ROUTE")].toString()));
			cb.setTWAYID((accident[accidentFields.indexOf("TWAY_ID")].toString()));
			cb.setTWAYID2((accident[accidentFields.indexOf("TWAY_ID2")].toString()));
			cb.setMILEPT((int)Double.parseDouble(accident[accidentFields.indexOf("MILEPT")].toString()));
			cb.setLATITUDE((int)Double.parseDouble(accident[accidentFields.indexOf("LATITUDE")].toString()));
			cb.setLONGITUD((int)Double.parseDouble(accident[accidentFields.indexOf("LONGITUD")].toString()));
			cb.setSPJUR((int)Double.parseDouble(accident[accidentFields.indexOf("SP_JUR")].toString()));
			cb.setHARMEV((int)Double.parseDouble(accident[accidentFields.indexOf("HARM_EV")].toString()));
			cb.setMANCOLL((int)Double.parseDouble(accident[accidentFields.indexOf("MAN_COLL")].toString()));
			cb.setRELJCT1((int)Double.parseDouble(accident[accidentFields.indexOf("RELJCT1")].toString()));
			cb.setRELJCT2((int)Double.parseDouble(accident[accidentFields.indexOf("RELJCT2")].toString()));
			cb.setTYPINT((int)Double.parseDouble(accident[accidentFields.indexOf("TYP_INT")].toString()));
			cb.setWRKZONE((int)Double.parseDouble(accident[accidentFields.indexOf("WRK_ZONE")].toString()));
			cb.setRELROAD((int)Double.parseDouble(accident[accidentFields.indexOf("REL_ROAD")].toString()));
			cb.setLGTCOND((int)Double.parseDouble(accident[accidentFields.indexOf("LGT_COND")].toString()));
			cb.setWEATHER1((int)Double.parseDouble(accident[accidentFields.indexOf("WEATHER1")].toString()));
			cb.setWEATHER2((int)Double.parseDouble(accident[accidentFields.indexOf("WEATHER2")].toString()));
			cb.setWEATHER((int)Double.parseDouble(accident[accidentFields.indexOf("WEATHER")].toString()));
			cb.setSCHBUS((int)Double.parseDouble(accident[accidentFields.indexOf("SCH_BUS")].toString()));
			cb.setRAIL((accident[accidentFields.indexOf("RAIL")].toString()));
			cb.setNOTHOUR((int)Double.parseDouble(accident[accidentFields.indexOf("NOT_HOUR")].toString()));
			cb.setNOTMIN((int)Double.parseDouble(accident[accidentFields.indexOf("NOT_MIN")].toString()));
			cb.setARRHOUR((int)Double.parseDouble(accident[accidentFields.indexOf("ARR_HOUR")].toString()));
			cb.setARRMIN((int)Double.parseDouble(accident[accidentFields.indexOf("ARR_MIN")].toString()));
			cb.setHOSPHR((int)Double.parseDouble(accident[accidentFields.indexOf("HOSP_HR")].toString()));
			cb.setHOSPMN((int)Double.parseDouble(accident[accidentFields.indexOf("HOSP_MN")].toString()));
			cb.setCF1((int)Double.parseDouble(accident[accidentFields.indexOf("CF1")].toString()));
			cb.setCF2((int)Double.parseDouble(accident[accidentFields.indexOf("CF2")].toString()));
			cb.setCF3((int)Double.parseDouble(accident[accidentFields.indexOf("CF3")].toString()));
			cb.setFATALS((int)Double.parseDouble(accident[accidentFields.indexOf("FATALS")].toString()));

			cb.setDRUNKDR((int)Double.parseDouble(accident[accidentFields.indexOf("DRUNK_DR")].toString()));

			Person.Builder pb = null;
			for (Object[] individual : persontData) {
				int index = personFields.indexOf("ST_CASE");
				if ((int)Double.parseDouble(individual[personFields.indexOf("ST_CASE")].toString()) == caseId) {
					pb = Person.newBuilder();
					pb.setState(getState((int)Double.parseDouble(individual[personFields.indexOf("STATE")].toString())));
					pb.setSTCASE((int)Double.parseDouble(individual[personFields.indexOf("ST_CASE")].toString()));
					pb.setVEFORMS((int)Double.parseDouble(individual[personFields.indexOf("VE_FORMS")].toString()));
					pb.setVEHNO((int)Double.parseDouble(individual[personFields.indexOf("VEH_NO")].toString()));
					pb.setPERNO((int)Double.parseDouble(individual[personFields.indexOf("PER_NO")].toString()));
					pb.setSTRVEH((int)Double.parseDouble(individual[personFields.indexOf("STR_VEH")].toString()));
					pb.setCOUNTY((int)Double.parseDouble(individual[personFields.indexOf("COUNTY")].toString()));
					pb.setDAY((int)Double.parseDouble(individual[personFields.indexOf("DAY")].toString()));
					pb.setMONTH((int)Double.parseDouble(individual[personFields.indexOf("MONTH")].toString()));
					pb.setHOUR((int)Double.parseDouble(individual[personFields.indexOf("HOUR")].toString()));
					pb.setMINUTE((int)Double.parseDouble(individual[personFields.indexOf("MINUTE")].toString()));
					pb.setRURURB((int)Double.parseDouble(individual[personFields.indexOf("RUR_URB")].toString()));
					pb.setFUNCSYS((int)Double.parseDouble(individual[personFields.indexOf("FUNC_SYS")].toString()));
					pb.setHARMEV((int)Double.parseDouble(individual[personFields.indexOf("HARM_EV")].toString()));
					pb.setMANCOLL((int)Double.parseDouble(individual[personFields.indexOf("MAN_COLL")].toString()));
					pb.setSCHBUS((int)Double.parseDouble(individual[personFields.indexOf("SCH_BUS")].toString()));
					pb.setMAKE((int)Double.parseDouble(individual[personFields.indexOf("MAKE")].toString()));
					pb.setMAKMOD((int)Double.parseDouble(individual[personFields.indexOf("MAK_MOD")].toString()));
					pb.setBODYTYP((int)Double.parseDouble(individual[personFields.indexOf("BODY_TYP")].toString()));
					pb.setMODYEAR((int)Double.parseDouble(individual[personFields.indexOf("MOD_YEAR")].toString()));
					pb.setTOWVEH((int)Double.parseDouble(individual[personFields.indexOf("TOW_VEH")].toString()));
					pb.setSPECUSE((int)Double.parseDouble(individual[personFields.indexOf("SPEC_USE")].toString()));
					pb.setEMERUSE((int)Double.parseDouble(individual[personFields.indexOf("EMER_USE")].toString()));
					pb.setROLLOVER((int)Double.parseDouble(individual[personFields.indexOf("ROLLOVER")].toString()));
					pb.setIMPACT1((int)Double.parseDouble(individual[personFields.indexOf("IMPACT1")].toString()));
					pb.setFIREEXP((int)Double.parseDouble(individual[personFields.indexOf("FIRE_EXP")].toString()));
					pb.setAGE((int)Double.parseDouble(individual[personFields.indexOf("AGE")].toString()));
					pb.setSEX((int)Double.parseDouble(individual[personFields.indexOf("SEX")].toString()));
					pb.setPERTYP((int)Double.parseDouble(individual[personFields.indexOf("PER_TYP")].toString()));
					pb.setINJSEV((int)Double.parseDouble(individual[personFields.indexOf("INJ_SEV")].toString()));
					pb.setSEATPOS((int)Double.parseDouble(individual[personFields.indexOf("SEAT_POS")].toString()));
					pb.setRESTUSE((int)Double.parseDouble(individual[personFields.indexOf("REST_USE")].toString()));
					pb.setRESTMIS((int)Double.parseDouble(individual[personFields.indexOf("REST_MIS")].toString()));
					pb.setAIRBAG((int)Double.parseDouble(individual[personFields.indexOf("AIR_BAG")].toString()));
					pb.setEJECTION((int)Double.parseDouble(individual[personFields.indexOf("EJECTION")].toString()));
					pb.setEJPATH((int)Double.parseDouble(individual[personFields.indexOf("EJ_PATH")].toString()));
					pb.setEXTRICAT((int)Double.parseDouble(individual[personFields.indexOf("EXTRICAT")].toString()));
					pb.setDRINKING((int)Double.parseDouble(individual[personFields.indexOf("DRINKING")].toString()));
					pb.setALCDET((int)Double.parseDouble(individual[personFields.indexOf("ALC_DET")].toString()));
					pb.setALCSTATUS((int)Double.parseDouble(individual[personFields.indexOf("ALC_STATUS")].toString()));
					pb.setATSTTYP((int)Double.parseDouble(individual[personFields.indexOf("ATST_TYP")].toString()));
					pb.setALCRES((int)Double.parseDouble(individual[personFields.indexOf("ALC_RES")].toString()));
					pb.setDRUGS((int)Double.parseDouble(individual[personFields.indexOf("DRUGS")].toString()));
					pb.setDRUGDET((int)Double.parseDouble(individual[personFields.indexOf("DRUG_DET")].toString()));
					pb.setDSTATUS((int)Double.parseDouble(individual[personFields.indexOf("DSTATUS")].toString()));
					pb.setDRUGTST1((int)Double.parseDouble(individual[personFields.indexOf("DRUGTST1")].toString()));
					pb.setDRUGTST2((int)Double.parseDouble(individual[personFields.indexOf("DRUGTST2")].toString()));
					pb.setDRUGTST3((int)Double.parseDouble(individual[personFields.indexOf("DRUGTST3")].toString()));
					pb.setDRUGRES1((int)Double.parseDouble(individual[personFields.indexOf("DRUGRES1")].toString()));
					pb.setDRUGRES2((int)Double.parseDouble(individual[personFields.indexOf("DRUGRES2")].toString()));
					pb.setDRUGRES3((int)Double.parseDouble(individual[personFields.indexOf("DRUGRES3")].toString()));
					pb.setHOSPITAL((int)Double.parseDouble(individual[personFields.indexOf("HOSPITAL")].toString()));
					pb.setDOA((int)Double.parseDouble(individual[personFields.indexOf("DOA")].toString()));
					pb.setDEATHDA((int)Double.parseDouble(individual[personFields.indexOf("DEATH_DA")].toString()));
					pb.setDEATHMO((int)Double.parseDouble(individual[personFields.indexOf("DEATH_MO")].toString()));
					pb.setDEATHYR((int)Double.parseDouble(individual[personFields.indexOf("DEATH_YR")].toString()));
					pb.setDEATHHR((int)Double.parseDouble(individual[personFields.indexOf("DEATH_HR")].toString()));
					pb.setDEATHMN((int)Double.parseDouble(individual[personFields.indexOf("DEATH_MN")].toString()));
					pb.setDEATHTM((int)Double.parseDouble(individual[personFields.indexOf("DEATH_TM")].toString()));
					pb.setLAGHRS((int)Double.parseDouble(individual[personFields.indexOf("LAG_HRS")].toString()));
					pb.setLAGMINS((int)Double.parseDouble(individual[personFields.indexOf("LAG_MINS")].toString()));
					pb.setPSF1((int)Double.parseDouble(individual[personFields.indexOf("P_SF1")].toString()));
					pb.setPSF2((int)Double.parseDouble(individual[personFields.indexOf("P_SF2")].toString()));
					pb.setPSF3((int)Double.parseDouble(individual[personFields.indexOf("P_SF3")].toString()));
					pb.setWORKINJ((int)Double.parseDouble(individual[personFields.indexOf("WORK_INJ")].toString()));
					pb.setHISPANIC((int)Double.parseDouble(individual[personFields.indexOf("HISPANIC")].toString()));
					pb.setRACE((int)Double.parseDouble(individual[personFields.indexOf("RACE")].toString()));
					pb.setLOCATION((int)Double.parseDouble(individual[personFields.indexOf("LOCATION")].toString()));
					cb.addPersons(pb.build());
				}
			}

			Vehicle.Builder vb = null;
			for (Object[] vehicle : vehicleData) {
				if ((int)Double.parseDouble(vehicle[personFields.indexOf("ST_CASE")].toString()) == caseId) {
					vb = Vehicle.newBuilder();
					vb.setState(getState((int)Double.parseDouble(vehicle[vehicleFields.indexOf("STATE")].toString())));
					vb.setSTCASE((int)Double.parseDouble(vehicle[vehicleFields.indexOf("ST_CASE")].toString()));
					vb.setVEHNO((int)Double.parseDouble(vehicle[vehicleFields.indexOf("VEH_NO")].toString()));
					vb.setVEFORMS((int)Double.parseDouble(vehicle[vehicleFields.indexOf("VE_FORMS")].toString()));
					vb.setNUMOCCS((int)Double.parseDouble(vehicle[vehicleFields.indexOf("NUMOCCS")].toString()));
					vb.setDAY((int)Double.parseDouble(vehicle[vehicleFields.indexOf("DAY")].toString()));
					vb.setMONTH((int)Double.parseDouble(vehicle[vehicleFields.indexOf("MONTH")].toString()));
					vb.setHOUR((int)Double.parseDouble(vehicle[vehicleFields.indexOf("HOUR")].toString()));
					vb.setMINUTE((int)Double.parseDouble(vehicle[vehicleFields.indexOf("MINUTE")].toString()));
					vb.setHARMEV((int)Double.parseDouble(vehicle[vehicleFields.indexOf("HARM_EV")].toString()));
					vb.setMANCOLL((int)Double.parseDouble(vehicle[vehicleFields.indexOf("MAN_COLL")].toString()));
					vb.setUNITTYPE((int)Double.parseDouble(vehicle[vehicleFields.indexOf("UNITTYPE")].toString()));
					vb.setHITRUN((int)Double.parseDouble(vehicle[vehicleFields.indexOf("HIT_RUN")].toString()));
					vb.setREGSTAT((int)Double.parseDouble(vehicle[vehicleFields.indexOf("REG_STAT")].toString()));
					vb.setOWNER((int)Double.parseDouble(vehicle[vehicleFields.indexOf("OWNER")].toString()));
					vb.setMAKE((int)Double.parseDouble(vehicle[vehicleFields.indexOf("MAKE")].toString()));
					vb.setMODEL((int)Double.parseDouble(vehicle[vehicleFields.indexOf("MODEL")].toString()));
					vb.setMAKMOD((int)Double.parseDouble(vehicle[vehicleFields.indexOf("MAK_MOD")].toString()));
					vb.setBODYTYP((int)Double.parseDouble(vehicle[vehicleFields.indexOf("BODY_TYP")].toString()));
					vb.setMODYEAR((int)Double.parseDouble(vehicle[vehicleFields.indexOf("MOD_YEAR")].toString()));
					vb.setVIN((vehicle[vehicleFields.indexOf("VIN")].toString()));
					vb.setVIN1((vehicle[vehicleFields.indexOf("VIN_1")].toString()));
					vb.setVIN2((vehicle[vehicleFields.indexOf("VIN_2")].toString()));
					vb.setVIN3((vehicle[vehicleFields.indexOf("VIN_3")].toString()));
					vb.setVIN4((vehicle[vehicleFields.indexOf("VIN_4")].toString()));
					vb.setVIN5((vehicle[vehicleFields.indexOf("VIN_5")].toString()));
					vb.setVIN6((vehicle[vehicleFields.indexOf("VIN_6")].toString()));
					vb.setVIN7((vehicle[vehicleFields.indexOf("VIN_7")].toString()));
					vb.setVIN8((vehicle[vehicleFields.indexOf("VIN_8")].toString()));
					vb.setVIN9((vehicle[vehicleFields.indexOf("VIN_9")].toString()));
					vb.setVIN10((vehicle[vehicleFields.indexOf("VIN_10")].toString()));
					vb.setVIN11((vehicle[vehicleFields.indexOf("VIN_11")].toString()));
					vb.setVIN12((vehicle[vehicleFields.indexOf("VIN_12")].toString()));
					vb.setTOWVEH((int)Double.parseDouble(vehicle[vehicleFields.indexOf("TOW_VEH")].toString()));
					vb.setJKNIFE((int)Double.parseDouble(vehicle[vehicleFields.indexOf("J_KNIFE")].toString()));
					vb.setMCARRI1((vehicle[vehicleFields.indexOf("MCARR_I1")].toString()));
					vb.setMCARRI2((vehicle[vehicleFields.indexOf("MCARR_I2")].toString()));
					vb.setMCARRID((vehicle[vehicleFields.indexOf("MCARR_ID")].toString()));
					vb.setGVWR((int)Double.parseDouble(vehicle[vehicleFields.indexOf("GVWR")].toString()));
					vb.setVCONFIG((int)Double.parseDouble(vehicle[vehicleFields.indexOf("V_CONFIG")].toString()));
					vb.setCARGOBT((int)Double.parseDouble(vehicle[vehicleFields.indexOf("CARGO_BT")].toString()));
					vb.setHAZINV((int)Double.parseDouble(vehicle[vehicleFields.indexOf("HAZ_INV")].toString()));
					vb.setHAZPLAC((int)Double.parseDouble(vehicle[vehicleFields.indexOf("HAZ_PLAC")].toString()));
					vb.setHAZID((int)Double.parseDouble(vehicle[vehicleFields.indexOf("HAZ_ID")].toString()));
					vb.setHAZCNO((int)Double.parseDouble(vehicle[vehicleFields.indexOf("HAZ_CNO")].toString()));
					vb.setHAZREL((int)Double.parseDouble(vehicle[vehicleFields.indexOf("HAZ_REL")].toString()));
					vb.setBUSUSE((int)Double.parseDouble(vehicle[vehicleFields.indexOf("BUS_USE")].toString()));
					vb.setSPECUSE((int)Double.parseDouble(vehicle[vehicleFields.indexOf("SPEC_USE")].toString()));
					vb.setEMERUSE((int)Double.parseDouble(vehicle[vehicleFields.indexOf("EMER_USE")].toString()));
					vb.setTRAVSP((int)Double.parseDouble(vehicle[vehicleFields.indexOf("TRAV_SP")].toString()));
					vb.setUNDERIDE((int)Double.parseDouble(vehicle[vehicleFields.indexOf("UNDERIDE")].toString()));
					vb.setROLLOVER((int)Double.parseDouble(vehicle[vehicleFields.indexOf("ROLLOVER")].toString()));
					vb.setROLINLOC((int)Double.parseDouble(vehicle[vehicleFields.indexOf("ROLINLOC")].toString()));
					vb.setIMPACT1((int)Double.parseDouble(vehicle[vehicleFields.indexOf("IMPACT1")].toString()));
					vb.setDEFORMED((int)Double.parseDouble(vehicle[vehicleFields.indexOf("DEFORMED")].toString()));
					vb.setTOWED((int)Double.parseDouble(vehicle[vehicleFields.indexOf("TOWED")].toString()));
					vb.setMHARM((int)Double.parseDouble(vehicle[vehicleFields.indexOf("M_HARM")].toString()));
					vb.setVEHSC1((int)Double.parseDouble(vehicle[vehicleFields.indexOf("VEH_SC1")].toString()));
					vb.setVEHSC2((int)Double.parseDouble(vehicle[vehicleFields.indexOf("VEH_SC2")].toString()));
					vb.setFIREEXP((int)Double.parseDouble(vehicle[vehicleFields.indexOf("FIRE_EXP")].toString()));
					vb.setDRPRES((int)Double.parseDouble(vehicle[vehicleFields.indexOf("DR_PRES")].toString()));
					vb.setLSTATE((int)Double.parseDouble(vehicle[vehicleFields.indexOf("L_STATE")].toString()));
					vb.setDRZIP((int)Double.parseDouble(vehicle[vehicleFields.indexOf("DR_ZIP")].toString()));
					vb.setLSTATUS((int)Double.parseDouble(vehicle[vehicleFields.indexOf("L_STATUS")].toString()));
					vb.setLTYPE((int)Double.parseDouble(vehicle[vehicleFields.indexOf("L_TYPE")].toString()));
					vb.setCDLSTAT((int)Double.parseDouble(vehicle[vehicleFields.indexOf("CDL_STAT")].toString()));
					vb.setLENDORS((int)Double.parseDouble(vehicle[vehicleFields.indexOf("L_ENDORS")].toString()));
					vb.setLCOMPL((int)Double.parseDouble(vehicle[vehicleFields.indexOf("L_COMPL")].toString()));
					vb.setLRESTRI((int)Double.parseDouble(vehicle[vehicleFields.indexOf("L_RESTRI")].toString()));
					vb.setDRHGT((int)Double.parseDouble(vehicle[vehicleFields.indexOf("DR_HGT")].toString()));
					vb.setDRWGT((int)Double.parseDouble(vehicle[vehicleFields.indexOf("DR_WGT")].toString()));
					vb.setPREVACC((int)Double.parseDouble(vehicle[vehicleFields.indexOf("PREV_ACC")].toString()));
					vb.setPREVSUS((int)Double.parseDouble(vehicle[vehicleFields.indexOf("PREV_SUS")].toString()));
					vb.setPREVDWI((int)Double.parseDouble(vehicle[vehicleFields.indexOf("PREV_DWI")].toString()));
					vb.setPREVSPD((int)Double.parseDouble(vehicle[vehicleFields.indexOf("PREV_SPD")].toString()));
					vb.setPREVOTH((int)Double.parseDouble(vehicle[vehicleFields.indexOf("PREV_OTH")].toString()));
					vb.setFIRSTMO((int)Double.parseDouble(vehicle[vehicleFields.indexOf("FIRST_MO")].toString()));
					vb.setFIRSTYR((int)Double.parseDouble(vehicle[vehicleFields.indexOf("FIRST_YR")].toString()));
					vb.setLASTMO((int)Double.parseDouble(vehicle[vehicleFields.indexOf("LAST_MO")].toString()));
					vb.setLASTYR((int)Double.parseDouble(vehicle[vehicleFields.indexOf("LAST_YR")].toString()));
					vb.setSPEEDREL((int)Double.parseDouble(vehicle[vehicleFields.indexOf("SPEEDREL")].toString()));
					vb.setDRSF1((int)Double.parseDouble(vehicle[vehicleFields.indexOf("DR_SF1")].toString()));
					vb.setDRSF2((int)Double.parseDouble(vehicle[vehicleFields.indexOf("DR_SF2")].toString()));
					vb.setDRSF3((int)Double.parseDouble(vehicle[vehicleFields.indexOf("DR_SF3")].toString()));
					vb.setDRSF4((int)Double.parseDouble(vehicle[vehicleFields.indexOf("DR_SF4")].toString()));
					vb.setVTRAFWAY((int)Double.parseDouble(vehicle[vehicleFields.indexOf("VTRAFWAY")].toString()));
					vb.setVNUMLAN((int)Double.parseDouble(vehicle[vehicleFields.indexOf("VNUM_LAN")].toString()));
					vb.setVSPDLIM((int)Double.parseDouble(vehicle[vehicleFields.indexOf("VSPD_LIM")].toString()));
					vb.setVALIGN((int)Double.parseDouble(vehicle[vehicleFields.indexOf("VALIGN")].toString()));
					vb.setVPROFILE((int)Double.parseDouble(vehicle[vehicleFields.indexOf("VPROFILE")].toString()));
					vb.setVPAVETYP((int)Double.parseDouble(vehicle[vehicleFields.indexOf("VPAVETYP")].toString()));
					vb.setVSURCOND((int)Double.parseDouble(vehicle[vehicleFields.indexOf("VSURCOND")].toString()));
					vb.setVTRAFCON((int)Double.parseDouble(vehicle[vehicleFields.indexOf("VTRAFCON")].toString()));
					vb.setVTCONTF((int)Double.parseDouble(vehicle[vehicleFields.indexOf("VTCONT_F")].toString()));
					vb.setPCRASH1((int)Double.parseDouble(vehicle[vehicleFields.indexOf("P_CRASH1")].toString()));
					vb.setPCRASH2((int)Double.parseDouble(vehicle[vehicleFields.indexOf("P_CRASH2")].toString()));
					vb.setPCRASH3((int)Double.parseDouble(vehicle[vehicleFields.indexOf("P_CRASH3")].toString()));
					vb.setPCRASH4((int)Double.parseDouble(vehicle[vehicleFields.indexOf("PCRASH4")].toString()));
					vb.setPCRASH5((int)Double.parseDouble(vehicle[vehicleFields.indexOf("PCRASH5")].toString()));
					vb.setACCTYPE((int)Double.parseDouble(vehicle[vehicleFields.indexOf("ACC_TYPE")].toString()));
					vb.setDEATHS((int)Double.parseDouble(vehicle[vehicleFields.indexOf("DEATHS")].toString()));
					vb.setDRDRINK((int)Double.parseDouble(vehicle[vehicleFields.indexOf("DR_DRINK")].toString()));
					cb.addVehicle(vb.build());
				}
}
			data.add(cb.build());
			System.out.println(count++);
		}
		return data;

	}

	private String getFilePath(String name) {
		name = name.endsWith(".dbf") ? name : name + ".dbf";
		for (String source : this.sources) {
			if (source.endsWith(name)) {
				return source;
			}
		}
		throw new IllegalArgumentException("No Such file exists in FARS");
	}

	public static void main(String[] args) {
		String accidentPath = "/Users/nmtiwari/git/research/boa_platform/trans_data/person.dbf";
		ArrayList<String> fields = new ArrayList<String>();
		DBF reader = new DBF(accidentPath);
		reader.getData(fields);
		System.out.println(fields.toString());
	}

	private STATE getState(int code) {
		return STATE.valueOf(code);
	}

}
