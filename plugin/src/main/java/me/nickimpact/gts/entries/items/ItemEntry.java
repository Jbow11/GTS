package me.nickimpact.gts.entries.items;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.nickimpact.impactor.api.configuration.ConfigKey;
import lombok.Getter;
import me.nickimpact.gts.GTS;
import me.nickimpact.gts.GTSInfo;
import me.nickimpact.gts.api.json.Typing;
import me.nickimpact.gts.api.listings.Listing;
import me.nickimpact.gts.api.listings.entries.Entry;
import me.nickimpact.gts.api.time.Time;
import me.nickimpact.gts.configuration.ConfigKeys;
import me.nickimpact.gts.configuration.MsgConfigKeys;
import me.nickimpact.gts.entries.prices.MoneyPrice;
import me.nickimpact.gts.internal.TextParsingUtils;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.InventoryTransformation;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.entity.Hotbar;
import org.spongepowered.api.item.inventory.entity.MainPlayerInventory;
import org.spongepowered.api.item.inventory.query.QueryOperationTypes;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.math.BigDecimal;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Represents an ItemStack that can be added as an element into the GTS market. The generified typing
 * here represents the lower-level phase of the ItemStack, as an ItemStack in a generic setup
 * like such produces a MalformedParameterizedTypeException, prevention serialization. Luckily
 * for us, Sponge provides a way to go from this low-level back up, so we can simply store the
 * data, then cache the ItemStack once it is recreated.
 *
 * @author NickImpact
 */
@Typing("Item")
public class ItemEntry extends Entry<DataContainer, ItemStack> {

	/** The cached version of the ItemStack */
	@Getter private transient ItemStack item;

	private String name;

	public ItemEntry() {
		super();
	}

	public ItemEntry(ItemStack element, MoneyPrice price) {
		super(element.toContainer(), price);
		this.item = element;
		this.name = element.getTranslation().get();
	}

	@Override
	protected ItemStack handle() {
		return this.decode();
	}

	private ItemStack decode() {
		return item != null ? item : (item = ItemStack.builder().fromContainer(this.element).build());
	}

	@Override
	public String getSpecsTemplate() {
		return GTS.getInstance().getMsgConfig().get(MsgConfigKeys.ITEM_ENTRY_SPEC_TEMPLATE);
	}

	@Override
	public String getName() {
		if(name == null) {
			ItemStack item = this.decode();
			return item.get(Keys.DISPLAY_NAME).orElse(Text.of(item.getTranslation().get())).toPlain();
		} else {
			return name;
		}
	}

	@Override
	public ItemStack baseItemStack(Player player, Listing listing) {
		ItemStack icon = ItemStack.builder()
				.fromItemStack(this.decode())
				.add(Keys.ITEM_ENCHANTMENTS, this.decode().get(Keys.ITEM_ENCHANTMENTS).orElse(Lists.newArrayList()))
				.build();

		List<String> lore = Lists.newArrayList();

		Map<String, Object> variables = Maps.newHashMap();
		variables.put("listing", listing);
		variables.put("item", this.decode());

		if(this.decode().get(Keys.DISPLAY_NAME).isPresent()) {
			Pattern pattern = Pattern.compile("[&][a-fk-or0-9]");
			Matcher matcher = pattern.matcher(TextSerializers.FORMATTING_CODE.serialize(this.decode().get(Keys.DISPLAY_NAME).get()));
			if(!matcher.find()) {
				icon.offer(Keys.DISPLAY_NAME, Text.of(TextColors.DARK_AQUA, decode().getTranslation().get(player.getLocale())));
				lore.add("&7Item Name: &e" + this.decode().get(Keys.DISPLAY_NAME).get().toPlain());
				lore.add("");
			} else {
				icon.offer(Keys.DISPLAY_NAME, TextParsingUtils.parse(GTS.getInstance().getMsgConfig().get(MsgConfigKeys.ITEM_ENTRY_BASE_TITLE), player, null, variables));
			}
		} else {
			icon.offer(Keys.DISPLAY_NAME, TextParsingUtils.parse(GTS.getInstance().getMsgConfig().get(MsgConfigKeys.ITEM_ENTRY_BASE_TITLE), player, null, variables));
		}

		lore.addAll(GTS.getInstance().getMsgConfig().get(MsgConfigKeys.ITEM_ENTRY_BASE_LORE));
		this.decode().get(Keys.ITEM_LORE).ifPresent(l -> {
			if(l.size() > 0) {
				lore.add("");
				lore.add("&aItem Lore: ");
				lore.addAll(l.stream().map(TextSerializers.FORMATTING_CODE::serialize).collect(Collectors.toList()));
			}
		});

		if(listing.getAucData() != null) {
			lore.addAll(GTS.getInstance().getMsgConfig().get(MsgConfigKeys.AUCTION_INFO));
		} else {
			lore.addAll(GTS.getInstance().getMsgConfig().get(MsgConfigKeys.ENTRY_INFO));
		}

		icon.offer(Keys.ITEM_LORE, TextParsingUtils.parse(lore, player, null, variables));

		return icon;
	}

	@Override
	public ItemStack confirmItemStack(Player player, Listing listing) {
		ItemStack icon = ItemStack.builder()
				.itemType(ItemTypes.PAPER)
				.build();

		Map<String, Object> variables = Maps.newHashMap();
		variables.put("listing", listing);
		variables.put("item", this.decode());

		icon.offer(Keys.DISPLAY_NAME, TextParsingUtils.parse(GTS.getInstance().getMsgConfig().get(MsgConfigKeys.ITEM_ENTRY_CONFIRM_TITLE), player, null, variables));
		List<String> lore = Lists.newArrayList();
		lore.addAll(GTS.getInstance().getMsgConfig().get(MsgConfigKeys.ITEM_ENTRY_CONFIRM_LORE));
		icon.offer(Keys.ITEM_LORE, TextParsingUtils.parse(lore, player, null, variables));

		return icon;
	}

	@Override
	public boolean supportsOffline() {
		return false;
	}

	@Override
	public boolean giveEntry(User user) {
		// User will always be a player here due to the offline support check
		Player player = (Player)user;
		if(player.getInventory().query(QueryOperationTypes.INVENTORY_TYPE.of(MainPlayerInventory.class)).size() == 36) {
			player.sendMessage(Text.of("Your inventory is full, so we can't award you this listing..."));
			return false;
		}

		player.getInventory().transform(InventoryTransformation.of(QueryOperationTypes.INVENTORY_TYPE.of(Hotbar.class), QueryOperationTypes.INVENTORY_TYPE.of(MainPlayerInventory.class))).offer(this.decode());
		return true;
	}

	@Override
	public boolean doTakeAway(Player player) {
		Optional<ItemStack> item = player.getInventory().query(QueryOperationTypes.INVENTORY_TYPE.of(MainPlayerInventory.class)).query(QueryOperationTypes.ITEM_STACK_IGNORE_QUANTITY.of(this.decode())).peek(this.decode().getQuantity());
		if(item.isPresent()) {
			if(!GTS.getInstance().getConfig().get(ConfigKeys.CUSTOM_NAME_ALLOWED)) {
				if(item.get().get(Keys.DISPLAY_NAME).isPresent()) {
					player.sendMessage(Text.of(GTSInfo.ERROR, TextColors.GRAY, "You can't sell items with custom names!"));
					return false;
				}
			}

			if(GTS.getInstance().getConfig().get(ConfigKeys.BLACKLISTED_ITEMS).contains(item.get().getType().getId())) {
				player.sendMessage(Text.of(GTSInfo.ERROR, TextColors.GRAY, "Sorry, but ", TextColors.YELLOW, this.getName(), TextColors.GRAY, " has been blacklisted from the GTS..."));
				return false;
			}

			player.getInventory().query(QueryOperationTypes.INVENTORY_TYPE.of(MainPlayerInventory.class)).query(QueryOperationTypes.ITEM_STACK_IGNORE_QUANTITY.of(this.decode())).poll(this.decode().getQuantity());
			return true;
		}
		return false;
	}

	public static CommandResult handleCommand(CommandSource src, String[] args) {
		if(args.length < 2) {
			return CommandResult.empty();
		}

		Player player = (Player) src;
		int amount = Integer.parseInt(args[0]);
		BigDecimal price = new BigDecimal(Double.parseDouble(args[1]));

		if(price.signum() <= 0) {
			player.sendMessage(Text.of(TextColors.RED, "Invalid price! Price values must be positive!"));
			return CommandResult.empty();
		}

		Time time = null;
		if(args.length == 3) {
			time = new Time(args[2]);
		}

		Optional<ItemStack> hand = player.getItemInHand(HandTypes.MAIN_HAND);
		if(!hand.isPresent()) {
			player.sendMessage(Text.of(TextColors.RED, "Your hand has no item in it!"));
			return CommandResult.empty();
		}

		if(amount < 1 || amount > hand.get().getQuantity()) {
			if(amount < 1) {
				amount = 1;
			} else {
				amount = hand.get().getQuantity();
			}
		}

		if(time != null) {
			if(time.getTime() > GTS.getInstance().getConfig().get(ConfigKeys.LISTING_MAX_TIME)) {
				time = new Time(GTS.getInstance().getConfig().get(ConfigKeys.LISTING_MAX_TIME).longValue());
			}
		}

		MoneyPrice mp = new MoneyPrice(price);
		if(!mp.isLowerOrEqual()) {
			player.sendMessage(Text.of(TextColors.RED, "Your request is above the max amount of ", new MoneyPrice(mp.getMax()).getText()));
			return CommandResult.success();
		}

		ItemStack entry = ItemStack.builder().fromItemStack(hand.get()).quantity(amount).build();
		Listing listing = Listing.builder()
				.entry(new ItemEntry(entry, mp))
				.doesExpire()
				.expiration(time != null ? time.getTime() : GTS.getInstance().getConfig().get(ConfigKeys.LISTING_TIME))
				.player(player)
				.build();
		listing.publish(player);

		return CommandResult.success();
	}
}
