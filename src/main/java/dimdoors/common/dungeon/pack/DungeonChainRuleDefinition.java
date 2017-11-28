package dimdoors.common.dungeon.pack;


import dimdoors.common.util.WeightedContainer;

import java.util.List;

public class DungeonChainRuleDefinition {
	private List<String> conditions;
	private List<WeightedContainer<String>> products;

	public DungeonChainRuleDefinition(List<String> conditions, List<WeightedContainer<String>> products) {
		//Validate the arguments, just in case
		if (conditions == null) {
			throw new NullPointerException("conditions cannot be null");
		}
		if (products.isEmpty()) {
			throw new IllegalArgumentException("products cannot be an empty list");
		}
		for (WeightedContainer<String> product : products) {
			//Check for weights less than 1. Those could cause Minecraft's random selection algorithm to throw an exception.
			//At the very least, they're useless values.
			if (product.itemWeight < 1) {
				throw new IllegalArgumentException("products cannot contain items with weights less than 1");
			}
		}

		this.conditions = conditions;
		this.products = products;
	}

	public List<String> getCondition() {
		return conditions;
	}

	public List<WeightedContainer<String>> getProducts() {
		return products;
	}

}
