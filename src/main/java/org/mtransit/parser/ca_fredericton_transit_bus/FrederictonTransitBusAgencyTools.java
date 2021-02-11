package org.mtransit.parser.ca_fredericton_transit_bus;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mtransit.commons.CharUtils;
import org.mtransit.commons.CleanUtils;
import org.mtransit.commons.StringUtils;
import org.mtransit.parser.DefaultAgencyTools;
import org.mtransit.parser.MTLog;
import org.mtransit.parser.gtfs.data.GRoute;
import org.mtransit.parser.mt.data.MAgency;

import java.util.Locale;
import java.util.regex.Pattern;

import static org.mtransit.commons.StringUtils.EMPTY;

// http://www.fredericton.ca/en/open-data
// http://data.fredericton.ca/en
// http://data-fredericton.opendata.arcgis.com/datasets/transit-routes--routes-de-transit
// http://gtransit.fredericton.ca/google_transit.zip
public class FrederictonTransitBusAgencyTools extends DefaultAgencyTools {

	public static void main(@NotNull String[] args) {
		new FrederictonTransitBusAgencyTools().start(args);
	}

	@Override
	public boolean defaultExcludeEnabled() {
		return true;
	}

	@NotNull
	@Override
	public Integer getAgencyRouteType() {
		return MAgency.ROUTE_TYPE_BUS;
	}

	@Override
	public long getRouteId(@NotNull GRoute gRoute) {
		if (CharUtils.isDigitsOnly(gRoute.getRouteShortName())) {
			return Long.parseLong(gRoute.getRouteShortName()); // use route short name as route ID
		}
		return super.getRouteId(gRoute);
	}

	@Nullable
	@Override
	public String getRouteShortName(@NotNull GRoute gRoute) {
		//noinspection deprecation
		return gRoute.getRouteId(); // use route ID as route short name
	}

	@NotNull
	@Override
	public String getRouteLongName(@NotNull GRoute gRoute) {
		String routeLongName = gRoute.getRouteLongNameOrDefault();
		routeLongName = CleanUtils.toLowerCaseUpperCaseWords(Locale.ENGLISH, routeLongName, getIgnoredWords());
		routeLongName = CleanUtils.cleanStreetTypes(routeLongName);
		return CleanUtils.cleanLabel(routeLongName);
	}

	private static final String AGENCY_COLOR_ORANGE = "FD6604"; // ORANGE (from PNG logo)

	private static final String AGENCY_COLOR = AGENCY_COLOR_ORANGE;

	@NotNull
	@Override
	public String getAgencyColor() {
		return AGENCY_COLOR;
	}

	@SuppressWarnings("DuplicateBranchesInSwitch")
	@Nullable
	@Override
	public String getRouteColor(@NotNull GRoute gRoute) {
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
			throw new MTLog.Fatal("Unexpected route long name for %s!", gRoute);
		}
		return super.getRouteColor(gRoute);
	}

	@Override
	public boolean directionFinderEnabled() {
		return true;
	}

	private static final Pattern STARTS_W_VIA_ = Pattern.compile("(^via .*$)", Pattern.CASE_INSENSITIVE);

	@NotNull
	@Override
	public String cleanDirectionHeadsign(boolean fromStopName, @NotNull String directionHeadSign) {
		directionHeadSign = CleanUtils.keepToAndRemoveVia(directionHeadSign);
		directionHeadSign = STARTS_W_VIA_.matcher(directionHeadSign).replaceAll(EMPTY); // remove trip only containing "via abc"
		directionHeadSign = super.cleanDirectionHeadsign(fromStopName, directionHeadSign);
		return directionHeadSign;
	}

	@NotNull
	@Override
	public String cleanTripHeadsign(@NotNull String tripHeadsign) {
		tripHeadsign = CleanUtils.keepVia(tripHeadsign, true);
		tripHeadsign = CleanUtils.cleanBounds(tripHeadsign);
		tripHeadsign = CleanUtils.cleanNumbers(tripHeadsign);
		tripHeadsign = CleanUtils.cleanStreetTypes(tripHeadsign);
		return CleanUtils.cleanLabel(tripHeadsign);
	}

	private String[] getIgnoredWords() {
		return new String[]{
				"DEC", "UNB",
		};
	}

	@NotNull
	@Override
	public String cleanStopName(@NotNull String gStopName) {
		gStopName = CleanUtils.toLowerCaseUpperCaseWords(Locale.ENGLISH, gStopName, getIgnoredWords());
		gStopName = CleanUtils.CLEAN_AND.matcher(gStopName).replaceAll(CleanUtils.CLEAN_AND_REPLACEMENT);
		gStopName = CleanUtils.CLEAN_AT.matcher(gStopName).replaceAll(CleanUtils.CLEAN_AT_REPLACEMENT);
		gStopName = CleanUtils.cleanSlashes(gStopName);
		gStopName = CleanUtils.cleanBounds(gStopName);
		gStopName = CleanUtils.cleanNumbers(gStopName);
		gStopName = CleanUtils.cleanStreetTypes(gStopName);
		return CleanUtils.cleanLabel(gStopName);
	}
}
