package mtr.packet;

import mtr.MTR;
import net.minecraft.resources.ResourceLocation;

public interface IPacket {

	ResourceLocation PACKET_VERSION_CHECK = MTR.id("packet_version_check");

	ResourceLocation PACKET_OPEN_DASHBOARD_SCREEN = MTR.id("packet_open_dashboard_screen");
	ResourceLocation PACKET_OPEN_PIDS_CONFIG_SCREEN = MTR.id("packet_open_pids_config_screen");
	ResourceLocation PACKET_OPEN_ARRIVAL_PROJECTOR_CONFIG_SCREEN = MTR.id("packet_open_arrival_projector_config_screen");
	ResourceLocation PACKET_OPEN_RAILWAY_SIGN_SCREEN = MTR.id("packet_open_railway_sign_screen");
	ResourceLocation PACKET_OPEN_TICKET_MACHINE_SCREEN = MTR.id("packet_open_ticket_machine_screen");
	ResourceLocation PACKET_OPEN_TRAIN_SENSOR_SCREEN = MTR.id("packet_open_train_sensor_screen");
	ResourceLocation PACKET_OPEN_LIFT_TRACK_FLOOR_SCREEN = MTR.id("packet_open_lift_track_floor_screen");
	ResourceLocation PACKET_OPEN_LIFT_CUSTOMIZATION_SCREEN = MTR.id("packet_open_lift_customization_screen");
	ResourceLocation PACKET_OPEN_RESOURCE_PACK_CREATOR_SCREEN = MTR.id("packet_open_resource_pack_creator_screen");

	ResourceLocation PACKET_ANNOUNCE = MTR.id("packet_announce");
	ResourceLocation PACKET_USE_TIME_AND_WIND_SYNC = MTR.id("packet_use_time_and_wind_sync");

	ResourceLocation PACKET_CREATE_RAIL = MTR.id("packet_create_rail");
	ResourceLocation PACKET_CREATE_SIGNAL = MTR.id("packet_create_signal");
	ResourceLocation PACKET_REMOVE_NODE = MTR.id("packet_remove_node");
	ResourceLocation PACKET_REMOVE_LIFT_FLOOR_TRACK = MTR.id("packet_remove_lift_floor_track");
	ResourceLocation PACKET_REMOVE_RAIL = MTR.id("packet_remove_rail");
	ResourceLocation PACKET_REMOVE_SIGNALS = MTR.id("packet_remove_signals");
	ResourceLocation PACKET_REMOVE_RAIL_ACTION = MTR.id("packet_remove_rail_action");

	ResourceLocation PACKET_GENERATE_PATH = MTR.id("packet_generate_path");
	ResourceLocation PACKET_CLEAR_TRAINS = MTR.id("packet_clear_trains");
	ResourceLocation PACKET_SIGN_TYPES = MTR.id("packet_sign_types");
	ResourceLocation PACKET_DRIVE_TRAIN = MTR.id("packet_drive_train");
	ResourceLocation PACKET_PRESS_LIFT_BUTTON = MTR.id("packet_press_lift_button");
	ResourceLocation PACKET_ADD_BALANCE = MTR.id("packet_add_balance");
	ResourceLocation PACKET_PIDS_UPDATE = MTR.id("packet_pids_update");
	ResourceLocation PACKET_ARRIVAL_PROJECTOR_UPDATE = MTR.id("packet_arrival_projector_update");
	ResourceLocation PACKET_CHUNK_S2C = MTR.id("packet_chunk_s2c");

	ResourceLocation PACKET_UPDATE_STATION = MTR.id("packet_update_station");
	ResourceLocation PACKET_UPDATE_PLATFORM = MTR.id("packet_update_platform");
	ResourceLocation PACKET_UPDATE_SIDING = MTR.id("packet_update_siding");
	ResourceLocation PACKET_UPDATE_ROUTE = MTR.id("packet_update_route");
	ResourceLocation PACKET_UPDATE_DEPOT = MTR.id("packet_update_depot");
	ResourceLocation PACKET_UPDATE_LIFT = MTR.id("packet_update_lift");

	ResourceLocation PACKET_DELETE_STATION = MTR.id("packet_delete_station");
	ResourceLocation PACKET_DELETE_PLATFORM = MTR.id("packet_delete_platform");
	ResourceLocation PACKET_DELETE_SIDING = MTR.id("packet_delete_siding");
	ResourceLocation PACKET_DELETE_ROUTE = MTR.id("packet_delete_route");
	ResourceLocation PACKET_DELETE_DEPOT = MTR.id("packet_delete_depot");

	ResourceLocation PACKET_WRITE_RAILS = MTR.id("write_rails");
	ResourceLocation PACKET_UPDATE_TRAINS = MTR.id("update_trains");
	ResourceLocation PACKET_DELETE_TRAINS = MTR.id("delete_trains");
	ResourceLocation PACKET_UPDATE_LIFTS = MTR.id("update_lifts");
	ResourceLocation PACKET_DELETE_LIFTS = MTR.id("delete_lifts");
	ResourceLocation PACKET_UPDATE_TRAIN_PASSENGERS = MTR.id("update_train_passengers");
	ResourceLocation PACKET_UPDATE_TRAIN_PASSENGER_POSITION = MTR.id("update_train_passenger_position");
	ResourceLocation PACKET_UPDATE_LIFT_PASSENGERS = MTR.id("update_lift_passengers");
	ResourceLocation PACKET_UPDATE_LIFT_PASSENGER_POSITION = MTR.id("update_lift_passenger_position");
	ResourceLocation PACKET_UPDATE_ENTITY_SEAT_POSITION = MTR.id("update_entity_seat_position");
	ResourceLocation PACKET_UPDATE_RAIL_ACTIONS = MTR.id("update_rail_actions");
	ResourceLocation PACKET_UPDATE_SCHEDULE = MTR.id("update_schedule");
	ResourceLocation PACKET_UPDATE_TRAIN_SENSOR = MTR.id("packet_update_train_announcer");
	ResourceLocation PACKET_UPDATE_LIFT_TRACK_FLOOR = MTR.id("packet_update_lift_track_floor");

	int MAX_PACKET_BYTES = 1048576;
}
