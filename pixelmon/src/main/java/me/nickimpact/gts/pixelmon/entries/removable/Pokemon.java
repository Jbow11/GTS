package me.nickimpact.gts.pixelmon.entries.removable;

import com.pixelmonmod.pixelmon.Pixelmon;
import com.pixelmonmod.pixelmon.config.PixelmonEntityList;
import com.pixelmonmod.pixelmon.entities.pixelmon.EntityPixelmon;
import me.nickimpact.gts.pixelmon.utils.GsonUtils;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import org.spongepowered.api.Sponge;

/**
 * (Some note will go here)
 *
 * @author NickImpact
 */
@Deprecated
public class Pokemon {

	private transient EntityPixelmon pokemon;

	private transient NBTTagCompound nbt;

	private String nbtJSON;

	public Pokemon(EntityPixelmon pokemon) {
		this.pokemon = pokemon;
		NBTTagCompound nbt = new NBTTagCompound();
		this.nbt = pokemon.writeToNBT(nbt);
		nbtJSON = this.nbt.toString();
	}

	public com.pixelmonmod.pixelmon.api.pokemon.Pokemon getPokemon() {
		return Pixelmon.pokemonFactory.create(this.decode());
	}

	private NBTTagCompound decode() {
		try {
			return nbt != null ? nbt : (nbt = JsonToNBT.getTagFromJson(nbtJSON));
		} catch (NBTException e) {
			return nbt = GsonUtils.deserialize(nbtJSON);
		}
	}
}
