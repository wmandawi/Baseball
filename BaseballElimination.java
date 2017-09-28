import java.util.HashMap;
import edu.princeton.cs.algs4.FordFulkerson;
import edu.princeton.cs.algs4.FlowNetwork;
import edu.princeton.cs.algs4.FlowEdge;
import java.util.ArrayList;
import java.util.Arrays;
public class BaseballElimination {
    private int[] w; //wins
    private int[] l; //losses
    private int[] r; //remaining
    private int[][] g; //games left against each
    private HashMap<String, Integer> sTon;
    private HashMap<Integer, String> nTos;
    private int N; //number of teams
    private String[] tm;
    private HashMap<String, ArrayList<String>> teams;
    private ArrayList<String> c = new ArrayList<String>();

    public BaseballElimination(String filename) {   // create a baseball division from given filename in format specified below
        In in = new In(filename);
        N = in.readInt();
        tm = new String[N];
        w = new int[N];
        l = new int[N];
        r = new int[N];
        g = new int[N][N];
        sTon = new HashMap<String, Integer>();
        nTos = new HashMap<Integer, String>();
        teams = new HashMap<String, ArrayList<String>>();
        for (int i = 0; i < N; i++) {
            tm[i] = in.readString();
            w[i] = in.readInt();
            l[i] = in.readInt();
            r[i] = in.readInt();
            sTon.put(tm[i], i);
            nTos.put(i, tm[i]);
            teams.put(tm[i], null);
            for (int j = 0; j < N; j++) {
                g[i][j] = in.readInt();
            }
        }
    }   

    public int numberOfTeams() {            // number of teams
        return N;
    }

    public Iterable<String> teams() {      // all teams
        return sTon.keySet();
    }

    public int wins(String team) {         // number of wins for given team
        if (!sTon.containsKey(team)) {
            throw new IllegalArgumentException("invalid team.");
        }
        return w[sTon.get(team)];
    }

    public int losses(String team) {      // number of losses for given team
        if (!sTon.containsKey(team)) {
            throw new IllegalArgumentException("invalid team.");
        }
        return l[sTon.get(team)];
    }

    public int remaining(String team) {  // number of remaining games for given team
        if (!sTon.containsKey(team)) {
            throw new IllegalArgumentException("invalid team.");
        }
        return r[sTon.get(team)];
    }

    public int against(String team1, String team2) {   // number of remaining games between team1 and team2
        if (!sTon.containsKey(team1) || !sTon.containsKey(team2)) {
            throw new IllegalArgumentException("one or both of the teams invalid.");
        }
        return g[sTon.get(team1)][sTon.get(team2)];
    }

    public boolean isEliminated(String team) {     // is given team eliminated?
        if (!sTon.containsKey(team)) {
            throw new IllegalArgumentException("invalid team.");
        }
        for (int i = 0; i < N; i++) {
            int x = sTon.get(team);
            if (i != x){
                if (w[x] + r[x] < w[i]) {

                    c.add(nTos.get(i));
                    return true;
                }
            }
        }

        if (flownetwork(team) != null){
            return true;
        }
        else return false;

    }


    private Iterable<String> flownetwork(String team) {
        int ng = N * (N - 1) / 2;
        int v = 1 + ng + N + 1;
        int s = v - 2;
        int t  = v - 1;
        FlowNetwork network = new FlowNetwork(v);
        int nOfv = 0;
        for (int i = 0; i < N; i++) {
            for (int j = i + 1; j < N; j++) { 
                if (i == sTon.get(team) && j == sTon.get(team)) {
                    network.addEdge(new FlowEdge(s, N+nOfv, g[i][j]));
                    network.addEdge(new FlowEdge(N+nOfv, i, Double.POSITIVE_INFINITY));
                    network.addEdge(new FlowEdge(N+nOfv, j, Double.POSITIVE_INFINITY));
                }
                nOfv++;
            }
            if ( i != sTon.get(team)) {
                network.addEdge(new FlowEdge(i, t, w[sTon.get(team)] + r[sTon.get(team)] - w[i]));
            } 
        }
        FordFulkerson ff = new FordFulkerson(network, s, t);

        for (int nOfV = 0; nOfV < N; nOfV++) {
            //int tv = ng + 1 + sTon.get(ct);
            if (ff.inCut(nOfV)) {
                c.add(tm[nOfV]);
            }
        }
        teams.put(team, c);
        if (c.isEmpty()) {
            return null;
        }
        return c;
    }

    public Iterable<String> certificateOfElimination(String team) { // subset R of teams that eliminates given team; null if not eliminated
        if (!sTon.containsKey(team)) {
            throw new IllegalArgumentException("invalid team.");
        }
        ArrayList<String> d = new ArrayList<String>();
         for (int i = 0; i < N; i++) {
            int x = sTon.get(team);
            if (i != x){
                if (w[x] + r[x] < w[i]) {
                    d.add(nTos.get(i));
                    return d;
                    //c.add(nTos.get(i));
                    
                }
            }
        }
        return flownetwork(team);
    }

    public static void main(String[] args) { //from assignment Page
        BaseballElimination division = new BaseballElimination(args[0]);
        for (String team : division.teams()) {
            if (division.isEliminated(team)) {
                StdOut.print(team + " is eliminated by the subset R = { ");
                for (String t : division.certificateOfElimination(team))
                    StdOut.print(t + " ");
                StdOut.println("}");
            }
            else {
                StdOut.println(team + " is not eliminated");
            }
        }
    }
}