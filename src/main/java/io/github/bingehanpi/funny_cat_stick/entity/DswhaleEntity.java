package io.github.bingehanpi.funny_cat_stick.entity;

import io.github.bingehanpi.funny_cat_stick.entity.ai.control.DswhaleBodyRotationControl;
import io.github.bingehanpi.funny_cat_stick.entity.ai.goal.DswhaleSleepGoal;
import io.github.bingehanpi.funny_cat_stick.entity.trade.DswhaleTradeOffers;
import io.github.bingehanpi.funny_cat_stick.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stats;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.control.BodyRotationControl;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MoveTowardsRestrictionGoal;
import net.minecraft.world.entity.ai.goal.OpenDoorGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.LookAtTradingPlayerGoal;
import net.minecraft.world.entity.ai.goal.TradeWithPlayerGoal;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

import javax.annotation.Nullable;
import java.util.Optional;

public final class DswhaleEntity extends AbstractVillager implements GeoEntity {

    private static final int MAX_HEAD_YAW = 60;
    private static final int HOME_RESTRICTION_RADIUS = 12;
    private static final int RESIDENCE_VALIDATION_INTERVAL = 20;
    private static final int INVALID_SLEEP_RETRY_DELAY = 100;
    private static final double SLEEP_HORIZONTAL_TOLERANCE = 0.55D;
    private static final double MIN_SLEEP_HEIGHT_ABOVE_BED = 0.45D;
    private static final double MAX_SLEEP_HEIGHT_ABOVE_BED = 0.85D;
    private static final double MAX_TRADING_DISTANCE_SQUARED = 16.0D;

    private static final EntityDataAccessor<Boolean> DATA_WEARING_IRON_POT =
            SynchedEntityData.defineId(DswhaleEntity.class, EntityDataSerializers.BOOLEAN);

    private static final String RESIDENCE_TAG = "FunnyCatStickResidence";
    private static final String DIMENSION_TAG = "Dimension";
    private static final String HOME_ANCHOR_TAG = "HomeAnchor";
    private static final String BED_TAG = "Bed";
    private static final String TRADE_XP_TAG = "FunnyCatStickTradeXp";
    private static final String SECOND_STAGE_TAG = "FunnyCatStickSecondTradeStage";
    private static final String SECOND_STAGE_OFFERS_TAG = "FunnyCatStickSecondStageOffersAdded";
    private static final String TREASURE_OFFER_TAG = "FunnyCatStickTreasureOfferAdded";
    private static final String LAST_RESTOCK_DAY_TAG = "FunnyCatStickLastRestockDay";
    private static final String WEARING_IRON_POT_TAG = "FunnyCatStickWearingIronPot";

    private static final RawAnimation IDLE_ANIMATION =
            RawAnimation.begin().thenLoop("animation.whale_maid_v2.idle");
    private static final RawAnimation WALK_ANIMATION =
            RawAnimation.begin().thenLoop("animation.whale_maid_v2.walk");
    private static final RawAnimation SLEEP_ANIMATION =
            RawAnimation.begin().thenLoop("animation.whale_maid_v2.sleep");

    private final AnimatableInstanceCache animationCache = GeckoLibUtil.createInstanceCache(this);
    private Residence residence;
    private long nextSleepAttemptTime;
    private int tradeXp;
    private boolean secondStageUnlocked;
    private boolean secondStageOffersAdded;
    private boolean treasureOfferAdded;
    private long lastRestockDay = Long.MIN_VALUE;

    public DswhaleEntity(EntityType<? extends DswhaleEntity> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 50.0D)
                .add(Attributes.ARMOR, 10.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.22D)
                .add(Attributes.FOLLOW_RANGE, 16.0D);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(DATA_WEARING_IRON_POT, false);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(1, new TradeWithPlayerGoal(this));
        goalSelector.addGoal(1, new LookAtTradingPlayerGoal(this));
        goalSelector.addGoal(2, new PanicGoal(this, 1.25D));
        goalSelector.addGoal(3, new OpenDoorGoal(this, true));
        goalSelector.addGoal(4, new DswhaleSleepGoal(this));
        goalSelector.addGoal(5, new MoveTowardsRestrictionGoal(this, 0.8D));
        goalSelector.addGoal(6, new RandomStrollGoal(this, 0.8D));
        goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 8.0F));
        goalSelector.addGoal(8, new RandomLookAroundGoal(this));
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        GroundPathNavigation navigation = new GroundPathNavigation(this, level);
        navigation.setCanOpenDoors(true);
        navigation.setCanPassDoors(true);
        return navigation;
    }

    @Override
    protected void customServerAiStep() {
        maintainTradingState();
        maintainSleepingState();
        if (tickCount % RESIDENCE_VALIDATION_INTERVAL == 0) {
            refreshResidenceRestriction();
        }
        super.customServerAiStep();
    }

    private void maintainTradingState() {
        Player tradingPlayer = getTradingPlayer();
        if (tradingPlayer == null) {
            return;
        }

        if (!tradingPlayer.isAlive()
                || tradingPlayer.isRemoved()
                || tradingPlayer.level() != level()
                || distanceToSqr(tradingPlayer) > MAX_TRADING_DISTANCE_SQUARED
                || !(tradingPlayer.containerMenu instanceof MerchantMenu)) {
            setTradingPlayer(null);
        }
    }

    private void maintainSleepingState() {
        if (!isSleeping()) {
            return;
        }

        Optional<BlockPos> sleepingPos = getSleepingPos();
        if (!level().isNight()) {
            stopSleeping();
            return;
        }

        if (sleepingPos.isEmpty() || !isValidBoundSleepingBed(sleepingPos.get())) {
            stopInvalidSleeping();
            return;
        }

        BlockPos bedPos = sleepingPos.get();
        BlockState bedState = level().getBlockState(bedPos);
        if (!bedState.getValue(BedBlock.OCCUPIED) || !isPositionedOnSleepingBed(bedPos)) {
            stopInvalidSleeping();
        }
    }

    private void stopInvalidSleeping() {
        nextSleepAttemptTime = level().getGameTime() + INVALID_SLEEP_RETRY_DELAY;
        stopSleeping();
    }

    private boolean isPositionedOnSleepingBed(BlockPos bedPos) {
        double offsetX = Math.abs(getX() - (bedPos.getX() + 0.5D));
        double offsetY = getY() - bedPos.getY();
        double offsetZ = Math.abs(getZ() - (bedPos.getZ() + 0.5D));
        return offsetX <= SLEEP_HORIZONTAL_TOLERANCE
                && offsetZ <= SLEEP_HORIZONTAL_TOLERANCE
                && offsetY >= MIN_SLEEP_HEIGHT_ABOVE_BED
                && offsetY <= MAX_SLEEP_HEIGHT_ABOVE_BED;
    }

    private boolean isValidBoundSleepingBed(BlockPos sleepingPos) {
        if (residence == null
                || !residence.bed().equals(sleepingPos)
                || !hasUsableResidence()
                || !level().hasChunkAt(sleepingPos)) {
            return false;
        }

        BlockState bedState = level().getBlockState(sleepingPos);
        return bedState.is(BlockTags.BEDS)
                && bedState.hasProperty(BedBlock.PART)
                && bedState.getValue(BedBlock.PART) == BedPart.HEAD
                && bedState.hasProperty(BedBlock.OCCUPIED);
    }

    public boolean setResidence(ResourceKey<Level> dimension, BlockPos homeAnchor, BlockPos bed) {
        if (level().isClientSide
                || !level().isInWorldBounds(homeAnchor)
                || !level().isInWorldBounds(bed)) {
            return false;
        }

        residence = new Residence(dimension, homeAnchor.immutable(), bed.immutable());
        refreshResidenceRestriction();
        return true;
    }

    public void clearResidence() {
        if (level().isClientSide) {
            return;
        }
        residence = null;
        clearRestriction();
    }

    public Optional<Residence> getResidence() {
        if (level().isClientSide) {
            return Optional.empty();
        }
        return Optional.ofNullable(residence);
    }

    public boolean canStartSleepingNow() {
        return !level().isClientSide && level().getGameTime() >= nextSleepAttemptTime;
    }

    public boolean hasUsableResidence() {
        if (level().isClientSide || residence == null || !level().dimension().equals(residence.dimension())) {
            return false;
        }

        BlockPos homeAnchor = residence.homeAnchor();
        BlockPos bed = residence.bed();
        if (!level().isInWorldBounds(homeAnchor)
                || !level().isInWorldBounds(bed)
                || !level().hasChunkAt(homeAnchor)
                || !level().hasChunkAt(bed)) {
            return false;
        }

        BlockPos aboveHome = homeAnchor.above();
        BlockPos belowHome = homeAnchor.below();
        return level().getBlockState(homeAnchor).isAir()
                && level().getBlockState(aboveHome).isAir()
                && level().getBlockState(belowHome).isFaceSturdy(level(), belowHome, Direction.UP)
                && level().getBlockState(bed).is(BlockTags.BEDS);
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack heldItem = player.getItemInHand(hand);
        if (isAlive() && heldItem.is(ModItems.IRON_POT.get()) && !isWearingIronPot()) {
            if (!level().isClientSide) {
                setWearingIronPot(true);
                if (!player.getAbilities().instabuild) {
                    heldItem.shrink(1);
                }
                playSound(SoundEvents.ARMOR_EQUIP_IRON, getSoundVolume(), getVoicePitch());
                ensureTreasureOffer();
            }
            return InteractionResult.sidedSuccess(level().isClientSide);
        }

        if (isAlive() && !isTrading() && !isSleeping() && !player.isSecondaryUseActive()) {
            boolean hasNoOffers = getOffers().isEmpty();
            if (hand == InteractionHand.MAIN_HAND) {
                player.awardStat(Stats.TALKED_TO_VILLAGER);
            }
            if (!hasNoOffers && !level().isClientSide) {
                setTradingPlayer(player);
                getNavigation().stop();
                openTradingScreen(player, getDisplayName(), getMerchantLevelForUi());
            }
            return InteractionResult.sidedSuccess(level().isClientSide);
        }

        return super.mobInteract(player, hand);
    }

    public boolean isWearingIronPot() {
        return entityData.get(DATA_WEARING_IRON_POT);
    }

    private void setWearingIronPot(boolean wearingIronPot) {
        entityData.set(DATA_WEARING_IRON_POT, wearingIronPot);
    }

    public boolean isSecondStageUnlocked() {
        return secondStageUnlocked;
    }

    @Override
    protected void updateTrades() {
        DswhaleTradeOffers.addFirstStageOffers(this, offers, random);
        if (secondStageUnlocked) {
            addSecondStageOffers();
        }
    }

    private void addSecondStageOffers() {
        if (secondStageOffersAdded || offers == null) {
            return;
        }
        DswhaleTradeOffers.addSecondStageOffers(offers, random);
        secondStageOffersAdded = true;
        ensureTreasureOffer();
    }

    private void ensureTreasureOffer() {
        if (!secondStageUnlocked || !isWearingIronPot() || treasureOfferAdded || offers == null) {
            return;
        }
        DswhaleTradeOffers.addTreasureOffer(offers);
        treasureOfferAdded = true;
        resendOffersToTradingPlayer();
    }

    @Override
    protected void rewardTradeXp(MerchantOffer offer) {
        if (secondStageUnlocked) {
            return;
        }

        tradeXp += offer.getXp();
        if (tradeXp >= DswhaleTradeOffers.SECOND_STAGE_TRADE_COUNT) {
            tradeXp = DswhaleTradeOffers.SECOND_STAGE_TRADE_COUNT;
            secondStageUnlocked = true;
            addSecondStageOffers();
        }
        resendOffersToTradingPlayer();
    }

    private void resendOffersToTradingPlayer() {
        Player tradingPlayer = getTradingPlayer();
        if (tradingPlayer != null && tradingPlayer.containerMenu instanceof MerchantMenu) {
            tradingPlayer.sendMerchantOffers(
                    tradingPlayer.containerMenu.containerId,
                    getOffers(),
                    getMerchantLevelForUi(),
                    getVillagerXp(),
                    showProgressBar(),
                    canRestock()
            );
        }
    }

    private int getMerchantLevelForUi() {
        return secondStageUnlocked ? 2 : 1;
    }

    @Override
    public int getVillagerXp() {
        return tradeXp;
    }

    @Override
    public void overrideXp(int xp) {
        tradeXp = Math.max(0, Math.min(xp, DswhaleTradeOffers.SECOND_STAGE_TRADE_COUNT));
    }

    @Override
    public boolean showProgressBar() {
        return !secondStageUnlocked;
    }

    @Override
    public boolean canRestock() {
        return true;
    }

    public void restock() {
        if (offers == null) {
            return;
        }
        for (MerchantOffer offer : offers) {
            offer.resetUses();
        }
        resendOffersToTradingPlayer();
    }

    private void restockAfterValidSleep() {
        long currentDay = level().getDayTime() / 24000L;
        if (currentDay == lastRestockDay) {
            return;
        }
        lastRestockDay = currentDay;
        if (offers != null && offers.stream().anyMatch(MerchantOffer::needsRestock)) {
            restock();
        }
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob partner) {
        return null;
    }

    @Override
    public boolean isClientSide() {
        return level().isClientSide;
    }

    @Override
    public SoundEvent getNotifyTradeSound() {
        return SoundEvents.VILLAGER_YES;
    }

    @Override
    public void notifyTradeUpdated(ItemStack result) {
        // The vanilla merchant menu calls this whenever the selected offer or
        // its input preview changes. Dswhale should not use villager voices.
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        if (residence != null) {
            CompoundTag residenceCompound = new CompoundTag();
            residenceCompound.putString(DIMENSION_TAG, residence.dimension().location().toString());
            residenceCompound.put(HOME_ANCHOR_TAG, NbtUtils.writeBlockPos(residence.homeAnchor()));
            residenceCompound.put(BED_TAG, NbtUtils.writeBlockPos(residence.bed()));
            compound.put(RESIDENCE_TAG, residenceCompound);
        }

        compound.putInt(TRADE_XP_TAG, tradeXp);
        compound.putBoolean(SECOND_STAGE_TAG, secondStageUnlocked);
        compound.putBoolean(SECOND_STAGE_OFFERS_TAG, secondStageOffersAdded);
        compound.putBoolean(TREASURE_OFFER_TAG, treasureOfferAdded);
        compound.putBoolean(WEARING_IRON_POT_TAG, isWearingIronPot());
        if (lastRestockDay != Long.MIN_VALUE) {
            compound.putLong(LAST_RESTOCK_DAY_TAG, lastRestockDay);
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        residence = readResidence(compound);
        tradeXp = Math.max(0, compound.getInt(TRADE_XP_TAG));
        secondStageUnlocked = compound.getBoolean(SECOND_STAGE_TAG)
                || tradeXp >= DswhaleTradeOffers.SECOND_STAGE_TRADE_COUNT;
        if (secondStageUnlocked) {
            tradeXp = Math.max(tradeXp, DswhaleTradeOffers.SECOND_STAGE_TRADE_COUNT);
        }
        secondStageOffersAdded = compound.getBoolean(SECOND_STAGE_OFFERS_TAG);
        treasureOfferAdded = compound.getBoolean(TREASURE_OFFER_TAG);
        setWearingIronPot(compound.getBoolean(WEARING_IRON_POT_TAG));
        lastRestockDay = compound.contains(LAST_RESTOCK_DAY_TAG, Tag.TAG_ANY_NUMERIC)
                ? compound.getLong(LAST_RESTOCK_DAY_TAG)
                : Long.MIN_VALUE;

        if (offers != null && secondStageUnlocked && !secondStageOffersAdded) {
            addSecondStageOffers();
        }
        if (offers != null) {
            ensureTreasureOffer();
        }
        if (!level().isClientSide) {
            refreshResidenceRestriction();
        }
    }

    private Residence readResidence(CompoundTag compound) {
        if (!compound.contains(RESIDENCE_TAG, Tag.TAG_COMPOUND)) {
            return null;
        }

        CompoundTag residenceCompound = compound.getCompound(RESIDENCE_TAG);
        if (!residenceCompound.contains(DIMENSION_TAG, Tag.TAG_STRING)
                || !hasBlockPos(residenceCompound, HOME_ANCHOR_TAG)
                || !hasBlockPos(residenceCompound, BED_TAG)) {
            return null;
        }

        ResourceLocation dimensionId = ResourceLocation.tryParse(residenceCompound.getString(DIMENSION_TAG));
        if (dimensionId == null) {
            return null;
        }

        BlockPos homeAnchor = NbtUtils.readBlockPos(residenceCompound.getCompound(HOME_ANCHOR_TAG));
        BlockPos bed = NbtUtils.readBlockPos(residenceCompound.getCompound(BED_TAG));
        if (!level().isInWorldBounds(homeAnchor) || !level().isInWorldBounds(bed)) {
            return null;
        }

        return new Residence(ResourceKey.create(Registries.DIMENSION, dimensionId), homeAnchor, bed);
    }

    private static boolean hasBlockPos(CompoundTag parent, String key) {
        if (!parent.contains(key, Tag.TAG_COMPOUND)) {
            return false;
        }
        CompoundTag position = parent.getCompound(key);
        return position.contains("X", Tag.TAG_ANY_NUMERIC)
                && position.contains("Y", Tag.TAG_ANY_NUMERIC)
                && position.contains("Z", Tag.TAG_ANY_NUMERIC);
    }

    private void refreshResidenceRestriction() {
        if (hasUsableResidence()) {
            restrictTo(residence.homeAnchor(), HOME_RESTRICTION_RADIUS);
        } else {
            clearRestriction();
        }
    }

    @Override
    public void stopSleeping() {
        Optional<BlockPos> sleepingPos = getSleepingPos();
        Direction bedDirection = getBedOrientation();
        boolean shouldRestock = !level().isClientSide
                && !level().isNight()
                && sleepingPos.filter(this::isValidBoundSleepingBed).isPresent();
        super.stopSleeping();

        if (!level().isClientSide && sleepingPos.isPresent() && bedDirection.getAxis().isHorizontal()) {
            BedBlock.findStandUpPosition(getType(), level(), sleepingPos.get(), bedDirection, getYRot())
                    .filter(this::canStandAt)
                    .or(this::getResidenceWakeFallback)
                    .ifPresent(wakePosition -> setPos(wakePosition.x, wakePosition.y, wakePosition.z));
        }
        if (shouldRestock) {
            restockAfterValidSleep();
        }
    }

    private boolean canStandAt(Vec3 position) {
        BlockPos feetPosition = BlockPos.containing(position);
        return !level().getBlockState(feetPosition).is(BlockTags.BEDS)
                && level().noCollision(this, getDimensions(Pose.STANDING).makeBoundingBox(position));
    }

    private Optional<Vec3> getResidenceWakeFallback() {
        if (residence == null
                || !level().dimension().equals(residence.dimension())
                || !level().isInWorldBounds(residence.homeAnchor())
                || !level().hasChunkAt(residence.homeAnchor())) {
            return Optional.empty();
        }

        BlockPos homeAnchor = residence.homeAnchor();
        Vec3 fallback = Vec3.atBottomCenterOf(homeAnchor);
        return canStandAt(fallback) ? Optional.of(fallback) : Optional.empty();
    }

    @Override
    protected BodyRotationControl createBodyControl() {
        return new DswhaleBodyRotationControl(this);
    }

    @Override
    public boolean isPushable() {
        return !isSleeping() && super.isPushable();
    }

    @Override
    public int getMaxHeadYRot() {
        return MAX_HEAD_YAW;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "movement", 5, state ->
                state.setAndContinue(isSleeping()
                        ? SLEEP_ANIMATION
                        : state.isMoving() ? WALK_ANIMATION : IDLE_ANIMATION)
        ));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return animationCache;
    }

    public record Residence(ResourceKey<Level> dimension, BlockPos homeAnchor, BlockPos bed) {
    }
}
