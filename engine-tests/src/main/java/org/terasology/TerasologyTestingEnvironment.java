// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine;

import com.badlogic.gdx.physics.bullet.Bullet;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.terasology.engine.context.Context;
import org.terasology.engine.core.ComponentSystemManager;
import org.terasology.engine.core.EngineTime;
import org.terasology.engine.core.Time;
import org.terasology.engine.core.bootstrap.EntitySystemSetupUtil;
import org.terasology.engine.core.modes.loadProcesses.LoadPrefabs;
import org.terasology.engine.core.module.ModuleManager;
import org.terasology.engine.core.paths.PathManager;
import org.terasology.engine.entitySystem.entity.internal.EngineEntityManager;
import org.terasology.engine.game.Game;
import org.terasology.engine.logic.console.Console;
import org.terasology.engine.logic.console.ConsoleImpl;
import org.terasology.engine.network.NetworkSystem;
import org.terasology.engine.network.internal.NetworkSystemImpl;
import org.terasology.engine.persistence.StorageManager;
import org.terasology.engine.persistence.internal.ReadWriteStorageManager;
import org.terasology.engine.recording.CharacterStateEventPositionMap;
import org.terasology.engine.recording.DirectionAndOriginPosRecorderList;
import org.terasology.engine.recording.RecordAndReplayCurrentStatus;
import org.terasology.engine.recording.RecordAndReplaySerializer;
import org.terasology.engine.recording.RecordAndReplayUtils;
import org.terasology.engine.recording.RecordedEventStore;
import org.terasology.engine.world.block.BlockManager;
import org.terasology.engine.world.chunks.blockdata.ExtraBlockDataManager;
import org.terasology.naming.Name;
import org.terasology.reflection.TypeRegistry;

import java.nio.file.Path;

import static org.mockito.Mockito.mock;

/**
 * A base class for unit test classes to inherit to run in a Terasology environment - with LWJGL set up and so forth
 *
 */
@ResourceLock(TestResourceLocks.CORE_REGISTRY)
public abstract class TerasologyTestingEnvironment implements MockedPathManager {
    protected  Context context;

    private  ModuleManager moduleManager;

    private HeadlessEnvironment env;

    protected EngineTime mockTime;
    private EngineEntityManager engineEntityManager;

    @BeforeEach
    public void setupEnvironment(@TempDir Path tempHome) throws Exception {
        PathManager.getInstance().useOverrideHomePath(tempHome);
        Bullet.init(true,false);

        /*
         * Create at least for each class a new headless environment as it is fast and prevents side effects
         * (Reusing a headless environment after other tests have modified the core registry isn't really clean)
         */
        env = new HeadlessEnvironment(new Name("engine"), new Name("unittest"));
        context = env.getContext();
        moduleManager = context.get(ModuleManager.class);

    }

    @BeforeEach
    public void setupTTE() throws Exception {

        context.put(ModuleManager.class, moduleManager);
        RecordAndReplayCurrentStatus recordAndReplayCurrentStatus = context.get(RecordAndReplayCurrentStatus.class);

        mockTime = mock(EngineTime.class);
        context.put(Time.class, mockTime);
        NetworkSystemImpl networkSystem = new NetworkSystemImpl(mockTime, context);
        context.put(Game.class, new Game());
        context.put(NetworkSystem.class, networkSystem);
        EntitySystemSetupUtil.addReflectionBasedLibraries(context);
        EntitySystemSetupUtil.addEntityManagementRelatedClasses(context);
        engineEntityManager = context.get(EngineEntityManager.class);
        BlockManager mockBlockManager = context.get(BlockManager.class); // 'mock' added to avoid hiding a field
        ExtraBlockDataManager extraDataManager = context.get(ExtraBlockDataManager.class);
        RecordedEventStore recordedEventStore = new RecordedEventStore();
        RecordAndReplayUtils recordAndReplayUtils = new RecordAndReplayUtils();
        context.put(RecordAndReplayUtils.class, recordAndReplayUtils);
        CharacterStateEventPositionMap characterStateEventPositionMap = new CharacterStateEventPositionMap();
        context.put(CharacterStateEventPositionMap.class, characterStateEventPositionMap);
        DirectionAndOriginPosRecorderList directionAndOriginPosRecorderList = new DirectionAndOriginPosRecorderList();
        context.put(DirectionAndOriginPosRecorderList.class, directionAndOriginPosRecorderList);
        RecordAndReplaySerializer recordAndReplaySerializer = new RecordAndReplaySerializer(engineEntityManager, recordedEventStore, recordAndReplayUtils, characterStateEventPositionMap, directionAndOriginPosRecorderList, moduleManager, context.get(TypeRegistry.class));
        context.put(RecordAndReplaySerializer.class, recordAndReplaySerializer);

        Path savePath = PathManager.getInstance().getSavePath("world1");
        context.put(StorageManager.class, new ReadWriteStorageManager(savePath, moduleManager.getEnvironment(),
                engineEntityManager, mockBlockManager, extraDataManager, recordAndReplaySerializer, recordAndReplayUtils, recordAndReplayCurrentStatus));

        ComponentSystemManager componentSystemManager = new ComponentSystemManager(context);
        context.put(ComponentSystemManager.class, componentSystemManager);
        LoadPrefabs prefabLoadStep = new LoadPrefabs(context);

        boolean complete = false;
        prefabLoadStep.begin();
        while (!complete) {
            complete = prefabLoadStep.step();
        }
        context.get(ComponentSystemManager.class).initialise();
        context.put(Console.class, new ConsoleImpl(context));
    }

    @AfterEach
    public void tearDown() throws Exception {
        env.close();
    }


    public EngineEntityManager getEntityManager() {
        return engineEntityManager;
    }
}
