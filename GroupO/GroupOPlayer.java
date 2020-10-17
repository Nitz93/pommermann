package players.GroupO;

import core.GameState;
import players.Player;
import players.GroupO.GroupOParams;
import players.GroupO.SingleTreeNodeGroupO;
import players.heuristics.AdvancedHeuristic;
import players.heuristics.CustomHeuristic;
import players.heuristics.StateHeuristic;
import players.optimisers.ParameterizedPlayer;
import utils.ElapsedCpuTimer;
import utils.Types;

import java.util.ArrayList;
import java.util.Random;

public class GroupOPlayer extends ParameterizedPlayer {

    /**
     * Random generator.
     */
    private Random m_rnd;
    private boolean rndOpponentModel;
    private StateHeuristic rootStateHeuristic;
    /**
     * All actions available.
     */
    public Types.ACTIONS[] actions;

    /**
     * Params for this MCTS
     */
    public GroupOParams params;

    public GroupOPlayer(long seed, int id) {
        this(seed, id, new GroupOParams());
    }

    public GroupOPlayer(long seed, int id, GroupOParams params) {
        super(seed, id, params);
        reset(seed, id);

        ArrayList<Types.ACTIONS> actionsList = Types.ACTIONS.all();
        actions = new Types.ACTIONS[actionsList.size()];
        int i = 0;
        for (Types.ACTIONS act : actionsList) {
            actions[i++] = act;
        }
    }

    @Override
    public void reset(long seed, int playerID) {
        super.reset(seed, playerID);
        m_rnd = new Random(seed);

        this.params = (GroupOParams) getParameters();
        if (this.params == null) {
            this.params = new GroupOParams();
            super.setParameters(this.params);
        }
    }

    @Override
    public Types.ACTIONS act(GameState gs) {

        // TODO update gs
        if (gs.getGameMode().equals(Types.GAME_MODE.TEAM_RADIO)){
            int[] msg = gs.getMessage();
        }

        ElapsedCpuTimer ect = new ElapsedCpuTimer();
        ect.setMaxTimeMillis(params.num_time);

        // Number of actions available
        int num_actions = actions.length;

        // Root of the tree
        SingleTreeNodeGroupO m_root = new SingleTreeNodeGroupO(params, m_rnd, num_actions, actions);
        m_root.setRootGameState(gs);

        GameState gsCopy = gs.copy();

        //Initializing the Tree
        m_root.initializeTree(gsCopy);

        //Determine the action using MCTS...
        m_root.mctsSearch(ect);

        //Determine the best action to take and return it.
        int action = m_root.mostVisitedAction();

        // TODO update message memory

        //... and return it.
        return actions[action];
    }

    @Override
    public int[] getMessage() {
        // default message
        int[] message = new int[Types.MESSAGE_LENGTH];
        message[0] = 1;
        return message;
    }

    @Override
    public Player copy() {
        return new GroupOPlayer(seed, playerID, params);
    }

    private void rollRnd(GameState gs, Types.ACTIONS act)
    {
        //Simple, all random first, then my position.
        int nPlayers = 4;
        Types.ACTIONS[] actionsAll = new Types.ACTIONS[4];

        for(int i = 0; i < nPlayers; ++i)
        {
            if(i == getPlayerID() - Types.TILETYPE.AGENT0.getKey())
            {
                actionsAll[i] = act;
            }else{
                if(rndOpponentModel){
                    int actionIdx = m_rnd.nextInt(gs.nActions());
                    actionsAll[i] = Types.ACTIONS.all().get(actionIdx);
                }else
                {
                    actionsAll[i] = Types.ACTIONS.ACTION_STOP;
                }
            }
        }

        gs.next(actionsAll);
    }
}