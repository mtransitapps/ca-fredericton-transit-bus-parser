package org.mtransit.parser.ca_fredericton_transit_bus;

import org.apache.commons.lang3.StringUtils;
import org.mtransit.parser.CleanUtils;
import org.mtransit.parser.DefaultAgencyTools;
import org.mtransit.parser.MTLog;
import org.mtransit.parser.Pair;
import org.mtransit.parser.SplitUtils;
import org.mtransit.parser.SplitUtils.RouteTripSpec;
import org.mtransit.parser.Utils;
import org.mtransit.parser.gtfs.data.GCalendar;
import org.mtransit.parser.gtfs.data.GCalendarDate;
import org.mtransit.parser.gtfs.data.GRoute;
import org.mtransit.parser.gtfs.data.GSpec;
import org.mtransit.parser.gtfs.data.GStop;
import org.mtransit.parser.gtfs.data.GTrip;
import org.mtransit.parser.gtfs.data.GTripStop;
import org.mtransit.parser.mt.data.MAgency;
import org.mtransit.parser.mt.data.MDirectionType;
import org.mtransit.parser.mt.data.MRoute;
import org.mtransit.parser.mt.data.MTrip;
import org.mtransit.parser.mt.data.MTripStop;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

// http://www.fredericton.ca/en/open-data
// http://data.fredericton.ca/en
// http://data-fredericton.opendata.arcgis.com/datasets/transit-routes--routes-de-transit
// http://gtransit.fredericton.ca/google_transit.zip
public class FrederictonTransitBusAgencyTools extends DefaultAgencyTools {

	public static void main(String[] args) {
		if (args == null || args.length == 0) {
			args = new String[4];
			args[0] = "input/gtfs.zip";
			args[1] = "../../mtransitapps/ca-fredericton-transit-bus-android/res/raw/";
			args[2] = ""; // files-prefix
			args[3] = Boolean.TRUE.toString(); // generate schedule from frequencies
		}
		new FrederictonTransitBusAgencyTools().start(args);
	}

	private HashSet<String> serviceIds;

	@Override
	public void start(String[] args) {
		MTLog.log("Generating Fredericton Transit bus data...");
		long start = System.currentTimeMillis();
		this.serviceIds = extractUsefulServiceIds(args, this, true);
		super.start(args);
		MTLog.log("Generating Fredericton Transit bus data... DONE in %s.", Utils.getPrettyDuration(System.currentTimeMillis() - start));
	}

	@Override
	public boolean excludingAll() {
		return this.serviceIds != null && this.serviceIds.isEmpty();
	}

	@Override
	public boolean excludeCalendar(GCalendar gCalendar) {
		if (this.serviceIds != null) {
			return excludeUselessCalendar(gCalendar, this.serviceIds);
		}
		return super.excludeCalendar(gCalendar);
	}

	@Override
	public boolean excludeCalendarDate(GCalendarDate gCalendarDates) {
		if (this.serviceIds != null) {
			return excludeUselessCalendarDate(gCalendarDates, this.serviceIds);
		}
		return super.excludeCalendarDate(gCalendarDates);
	}

	@Override
	public boolean excludeTrip(GTrip gTrip) {
		if (this.serviceIds != null) {
			return excludeUselessTrip(gTrip, this.serviceIds);
		}
		return super.excludeTrip(gTrip);
	}

	@Override
	public boolean excludeRoute(GRoute gRoute) {
		return super.excludeRoute(gRoute);
	}

	@Override
	public Integer getAgencyRouteType() {
		return MAgency.ROUTE_TYPE_BUS;
	}

	private static final String RSN_11 = "11";
	private static final String RSN_13 = "13";
	private static final String RSN_15 = "15";
	private static final String RSN_16 = "16";

	private static final String RID_11N = "11N";
	private static final String RID_13N = "13N";
	private static final String RID_15N = "15N";
	private static final String RID_16S = "16S";

	@Override
	public long getRouteId(GRoute gRoute) {
		//noinspection deprecation
		final String routeId = gRoute.getRouteId();
		if (RID_11N.equals(routeId)
				&& RSN_11.equals(gRoute.getRouteShortName())) {
			return 10L;
		} else if (RID_13N.equals(routeId)
				&& RSN_13.equals(gRoute.getRouteShortName())) {
			return 12L;
		} else if (RID_15N.equals(routeId)
				&& RSN_15.equals(gRoute.getRouteShortName())) {
			return 14L;
		} else if (RID_16S.equals(routeId)
				&& RSN_16.equals(gRoute.getRouteShortName())) {
			return 17L;
		}
		return Long.parseLong(gRoute.getRouteShortName()); // use route short name as route ID
	}

	private static final String _10N_RSN = "10N";
	private static final String _12N_RSN = "12N";
	private static final String _14N_RSN = "14N";
	private static final String _17S_RSN = "17S";

	@Override
	public String getRouteShortName(GRoute gRoute) {
		//noinspection deprecation
		final String routeId = gRoute.getRouteId();
		if (RID_11N.equals(routeId)
				&& RSN_11.equals(gRoute.getRouteShortName())) {
			return _10N_RSN;
		} else if (RID_13N.equals(routeId)
				&& RSN_13.equals(gRoute.getRouteShortName())) {
			return _12N_RSN;
		} else if (RID_15N.equals(routeId)
				&& RSN_15.equals(gRoute.getRouteShortName())) {
			return _14N_RSN;
		} else if (RID_16S.equals(routeId)
				&& RSN_16.equals(gRoute.getRouteShortName())) {
			return _17S_RSN;
		}
		return routeId; // use route ID as route short name
	}

	@Override
	public String getRouteLongName(GRoute gRoute) {
		String routeLongName = gRoute.getRouteLongNameOrDefault().toLowerCase(Locale.ENGLISH);
		routeLongName = CleanUtils.cleanStreetTypes(routeLongName);
		return CleanUtils.cleanLabel(routeLongName);
	}

	private static final String AGENCY_COLOR_ORANGE = "FD6604"; // ORANGE (from PNG logo)

	private static final String AGENCY_COLOR = AGENCY_COLOR_ORANGE;

	@Override
	public String getAgencyColor() {
		return AGENCY_COLOR;
	}

	@SuppressWarnings("DuplicateBranchesInSwitch")
	@Override
	public String getRouteColor(GRoute gRoute) {
		if (StringUtils.isEmpty(gRoute.getRouteColor())) {
			int rid = (int) getRouteId(gRoute);
			switch (rid) {
			// @formatter:off
			case 10: return "75923C"; // Dark Green
			case 11: return "75923C"; // Dark Green
			case 12: return "4169E1"; // Blue
			case 13: return "4169E1"; // Blue
			case 14: return "E60000"; // Red
			case 15: return "E60000"; // Red
			case 16: return "32CD32"; // Green
			case 17: return "32CD32"; // Green
			case 18: return "996633"; // Purple
			case 20: return "996633"; // Purple
			case 116: return "4B0082"; // Brown
			case 216: return "4B0082"; // Brown
			// @formatter:on
			}
			MTLog.logFatal("Unexpected route long name for %s!", gRoute);
			return null;
		}
		return super.getRouteColor(gRoute);
	}

	private static HashMap<Long, RouteTripSpec> ALL_ROUTE_TRIPS2;

	static {
		//noinspection UnnecessaryLocalVariable
		HashMap<Long, RouteTripSpec> map2 = new HashMap<>();
		ALL_ROUTE_TRIPS2 = map2;
	}

	@Override
	public int compareEarly(long routeId, List<MTripStop> list1, List<MTripStop> list2, MTripStop ts1, MTripStop ts2, GStop ts1GStop, GStop ts2GStop) {
		if (ALL_ROUTE_TRIPS2.containsKey(routeId)) {
			return ALL_ROUTE_TRIPS2.get(routeId).compare(routeId, list1, list2, ts1, ts2, ts1GStop, ts2GStop, this);
		}
		return super.compareEarly(routeId, list1, list2, ts1, ts2, ts1GStop, ts2GStop);
	}

	@Override
	public ArrayList<MTrip> splitTrip(MRoute mRoute, GTrip gTrip, GSpec gtfs) {
		if (ALL_ROUTE_TRIPS2.containsKey(mRoute.getId())) {
			return ALL_ROUTE_TRIPS2.get(mRoute.getId()).getAllTrips();
		}
		return super.splitTrip(mRoute, gTrip, gtfs);
	}

	@Override
	public Pair<Long[], Integer[]> splitTripStop(MRoute mRoute, GTrip gTrip, GTripStop gTripStop, ArrayList<MTrip> splitTrips, GSpec routeGTFS) {
		if (ALL_ROUTE_TRIPS2.containsKey(mRoute.getId())) {
			return SplitUtils.splitTripStop(mRoute, gTrip, gTripStop, routeGTFS, ALL_ROUTE_TRIPS2.get(mRoute.getId()), this);
		}
		return super.splitTripStop(mRoute, gTrip, gTripStop, splitTrips, routeGTFS);
	}

	private static final String RID_ENDS_WITH_N = "N";
	private static final String RID_ENDS_WITH_S = "S";

	@Override
	public void setTripHeadsign(MRoute mRoute, MTrip mTrip, GTrip gTrip, GSpec gtfs) {
		if (ALL_ROUTE_TRIPS2.containsKey(mRoute.getId())) {
			return; // split
		}
		if (gTrip.getRouteId().endsWith(RID_ENDS_WITH_N)) {
			mTrip.setHeadsignDirection(MDirectionType.NORTH);
			return;
		} else if (gTrip.getRouteId().endsWith(RID_ENDS_WITH_S)) {
			mTrip.setHeadsignDirection(MDirectionType.SOUTH);
			return;
		}
		if (mRoute.getId() == 18L) {
			mTrip.setHeadsignDirection(MDirectionType.WEST);
			return;
		} else if (mRoute.getId() == 20L) {
			mTrip.setHeadsignDirection(MDirectionType.EAST);
			return;
		} else if (mRoute.getId() == 116L) {
			mTrip.setHeadsignDirection(MDirectionType.NORTH);
			return;
		} else if (mRoute.getId() == 216L) {
			mTrip.setHeadsignDirection(MDirectionType.SOUTH);
			return;
		}
		mTrip.setHeadsignString(cleanTripHeadsign(gTrip.getTripHeadsign()), gTrip.getDirectionId() == null ? 0 : gTrip.getDirectionId());
	}

	@Override
	public String cleanTripHeadsign(String tripHeadsign) {
		tripHeadsign = CleanUtils.removeVia(tripHeadsign);
		tripHeadsign = CleanUtils.removePoints(tripHeadsign);
		tripHeadsign = CleanUtils.cleanNumbers(tripHeadsign);
		tripHeadsign = CleanUtils.cleanStreetTypes(tripHeadsign);
		return CleanUtils.cleanLabel(tripHeadsign);
	}

	@Override
	public String cleanStopName(String gStopName) {
		gStopName = CleanUtils.CLEAN_AND.matcher(gStopName).replaceAll(CleanUtils.CLEAN_AND_REPLACEMENT);
		gStopName = CleanUtils.CLEAN_AT.matcher(gStopName).replaceAll(CleanUtils.CLEAN_AT_REPLACEMENT);
		gStopName = CleanUtils.cleanSlashes(gStopName);
		gStopName = CleanUtils.removePoints(gStopName);
		gStopName = CleanUtils.cleanNumbers(gStopName);
		gStopName = CleanUtils.cleanStreetTypes(gStopName);
		return CleanUtils.cleanLabel(gStopName);
	}
}
