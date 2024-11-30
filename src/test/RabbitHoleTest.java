package test;

class RabbitHoleTest {
    //TODO : MAKE RABBITHOLE TEST AGAIN
    /*
    OldRabbitHole hole;

    @BeforeEach
    void setUp() {
        hole = new OldRabbitHole();
    }

    @Test
    void connectHole() {
        OldRabbitHole connected = new OldRabbitHole(hole);
        hole.connectHole(connected);

        assertEquals(hole.getConnectedHoles(), connected.getConnectedHoles());
    }

    @Test
    void disconnectHole() {
        OldRabbitHole connected = new OldRabbitHole(hole);
        hole.connectHole(connected);
        hole.disconnectHole(connected);

        assertEquals(1, hole.getConnectedHoles().size());
    }

    @Test
    void animalEnters() {
        Animal rabbit = new Rabbit();
        hole.rabbitEntersNetwork(rabbit);

        assertEquals(1, hole.getInhabitants().size());
    }

    @Test
    void animalLeave() {
        Animal rabbit = new Rabbit();
        hole.rabbitEntersNetwork(rabbit);
        assertEquals(1, hole.getInhabitants().size());

        hole.rabbitExitsNetwork(rabbit);
        assertEquals(0, hole.getInhabitants().size());
    }

    @Test
    void getLocation() {
        World world = new World(5);
        Location l = new Location(0, 0);
        world.setCurrentLocation(l);
        world.setTile(l, hole);
        assertEquals(0, hole.getLocation(world).getX());
        assertEquals(0, hole.getLocation(world).getY());
    }

    @Test
    void destroyHole() {
        World world = new World(5);
        Location l = new Location(0, 0);
        world.setCurrentLocation(l);
        world.setTile(l, hole);
        for (int i = 0; i < 10; i++) {
            hole.connectHole(new OldRabbitHole(hole.getConnectedHoles()));
            assertEquals(i+2, hole.getConnectedHoles().size());
        }
        Set<OldRabbitHole> connectedHoles = hole.getConnectedHoles();
        hole.destroyHole(world);
        for (OldRabbitHole connected : connectedHoles) {
            if (connected.equals(hole)) continue;
            assertFalse(connected.getConnectedHoles().contains(hole));
        }
        assertNull(hole.getConnectedHoles());
        assertFalse(world.contains(hole));
    }

    */
}
