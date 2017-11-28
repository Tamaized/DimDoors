package dimdoors.common.watcher;


import dimdoors.common.core.DimType;
import dimdoors.common.core.NewDimData;
import io.netty.buffer.ByteBuf;

import java.io.IOException;

public class ClientDimData {

	//We'll use public fields since this is just a data container and it's immutable
	public final int ID;
	public final int rootID;
	public final DimType type;

	public ClientDimData(int id, int rootID, DimType type) {
		ID = id;
		this.rootID = rootID;
		this.type = type;
	}

	public ClientDimData(NewDimData dimension) {
		ID = dimension.id();
		this.rootID = dimension.root().id();
		this.type = dimension.type();
	}

	public static ClientDimData read(ByteBuf input) throws IOException {
		int id = input.readInt();
		int rootID = input.readInt();
		int index = input.readInt();
		return new ClientDimData(id, rootID, DimType.getTypeFromIndex(index));
	}

	public void write(ByteBuf output) throws IOException {
		output.writeInt(ID);
		output.writeInt(rootID);
		output.writeInt(type.index);
	}
}
