package dimdoors.network;

import dimdoors.common.core.DimType;
import dimdoors.common.core.NewDimData;

public interface IDimRegistrationCallback {

	NewDimData registerDimension(int dimensionID, int rootID, DimType type);

}
