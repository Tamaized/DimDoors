package dimdoors.common.world.gateways;

public class GatewaySandstonePillars extends BaseSchematicGateway {

	public GatewaySandstonePillars() {
		super();
	}

	@Override
	public String[] getBiomeKeywords() {
		return new String[]{"desert"};
	}

	@Override
	public String getSchematicPath() {
		return "/schematics/gateways/sandstonePillars.schematic";
	}
}
