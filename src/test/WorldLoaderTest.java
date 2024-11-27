package test;

import org.junit.jupiter.api.Test;
import simulator.actors.Animal;
import simulator.actors.Rabbit;
import simulator.objects.NonBlockable;
import simulator.objects.holes.OldRabbitHole;
import simulator.objects.plants.Grass;
import simulator.util.WorldLoader;

import java.util.List;


import static org.junit.jupiter.api.Assertions.*;

class WorldLoaderTest {
    WorldLoader wl;

    @Test
    public void t1_1a() {
        try{
            this.wl = new WorldLoader("resources/inputs/week-1/t1-1a.txt", 200, 200);
        }catch(Exception e) {
            fail("Input file t1-1a.txt not found");
        }

        assertTrue(wl.getWorldSize() == 5);
        assertTrue(wl.getNonBlockables().size() == 3);
        assertTrue(wl.getAnimals().size() == 0);

        List<NonBlockable> nonBlockables = wl.getNonBlockables();
        for(NonBlockable nb : nonBlockables) {
            if(!(nb instanceof Grass)) {
                fail("Incorrect NonBlockable dynamic type, expected grass");
            }
        }
    }

    @Test
    public void t1_1b() {
        try{
            this.wl = new WorldLoader("resources/inputs/week-1/t1-1b.txt", 200, 200);
        }catch(Exception e) {
            fail("Input file t1-1b.txt not found");
        }

        assertTrue(wl.getWorldSize() == 10);
        assertTrue(wl.getNonBlockables().size() == 1);
        assertTrue(wl.getAnimals().size() == 0);

        List<NonBlockable> nonBlockables = wl.getNonBlockables();
        for(NonBlockable nb : nonBlockables) {
            if(!(nb instanceof Grass)) {
                fail("Incorrect NonBlockable dynamic type, expected grass");
            }
        }
    }

    @Test
    public void t1_1c() {
        try{
            this.wl = new WorldLoader("resources/inputs/week-1/t1-1c.txt", 200, 200);
        }catch(Exception e) {
            System.out.println(e.getMessage());
            fail("Input file t1-1c.txt not found");
        }


        List<NonBlockable> nonBlockables = wl.getNonBlockables();

        assertTrue(wl.getWorldSize() == 10);
        assertTrue(nonBlockables.size() >= 20 && nonBlockables.size() <= 30);
        assertTrue(wl.getAnimals().size() == 1);

        for(NonBlockable nb : nonBlockables) {
            if(!(nb instanceof Grass)) {
                fail("Incorrect NonBlockable dynamic type, expected grass");
            }
        }

        for(Animal animal : wl.getAnimals()) {
            if(!(animal instanceof Rabbit)) {
                fail("Incorrect Animal dynamic type, expected rabbit");
            }
        }
    }

    @Test
    public void t1_2a() {
        try{
            this.wl = new WorldLoader("resources/inputs/week-1/t1-2a.txt", 200, 200);
        }catch(Exception e) {
            fail("Input file t1-2a.txt not found");
        }


        List<NonBlockable> nonBlockables = wl.getNonBlockables();

        assertTrue(wl.getWorldSize() == 5);
        assertTrue(nonBlockables.size() == 0);
        assertTrue(wl.getAnimals().size() == 1);

        for(Animal animal : wl.getAnimals()) {
            if(!(animal instanceof Rabbit)) {
                fail("Incorrect Animal dynamic type, expected rabbit");
            }
        }
    }

    @Test
    public void t1_2b() {
        try{
            this.wl = new WorldLoader("resources/inputs/week-1/t1-2b.txt", 200, 200);
        }catch(Exception e) {
            fail("Input file t1-2b.txt not found");
        }

        List<NonBlockable> nonBlockables = wl.getNonBlockables();
        List<Animal> animals = wl.getAnimals();

        assertTrue(wl.getWorldSize() == 5);
        assertTrue(nonBlockables.size() == 0);
        assertTrue(animals.size() == 2);

        for(Animal animal : wl.getAnimals()) {
            if(!(animal instanceof Rabbit)) {
                fail("Incorrect Animal dynamic type, expected rabbit");
            }
        }
    }

    @Test
    public void t1_2cde() {
        try{
            this.wl = new WorldLoader("resources/inputs/week-1/t1-2cde.txt", 200, 200);
        }catch(Exception e) {
            fail("Input file t1-2cde.txt not found");
        }

        List<NonBlockable> nonBlockables = wl.getNonBlockables();
        List<Animal> animals = wl.getAnimals();

        assertTrue(wl.getWorldSize() == 10);
        assertTrue(nonBlockables.size() >= 3 && nonBlockables.size() <= 10);
        assertTrue(animals.size() >= 2 && animals.size() <= 4);

        for(Animal animal : wl.getAnimals()) {
            if(!(animal instanceof Rabbit)) {
                fail("Incorrect Animal dynamic type, expected rabbit");
            }
        }
        for(NonBlockable animal : wl.getNonBlockables()) {
            if(!(animal instanceof Grass)) {
                fail("Incorrect NonBlockable dynamic type, expected Grass");
            }
        }
    }

    @Test
    public void t1_2fg() {
        try{
            this.wl = new WorldLoader("resources/inputs/week-1/t1-2fg.txt", 200, 200);
        }catch(Exception e) {
            fail("Input file t1-2fg.txt not found");
        }

        List<NonBlockable> nonBlockables = wl.getNonBlockables();
        List<Animal> animals = wl.getAnimals();

        assertTrue(wl.getWorldSize() == 15);
        assertTrue(nonBlockables.size() == 0);
        assertTrue(animals.size() == 4);

        for(Animal animal : wl.getAnimals()) {
            if(!(animal instanceof Rabbit)) {
                fail("Incorrect Animal dynamic type, expected rabbit");
            }
        }

    }

    @Test
    public void t1_3a() {
        try{
            this.wl = new WorldLoader("resources/inputs/week-1/t1-3a.txt", 200, 200);
        }catch(Exception e) {
            fail("Input file t1-3a.txt not found");
        }

        List<NonBlockable> nonBlockables = wl.getNonBlockables();
        List<Animal> animals = wl.getAnimals();

        assertTrue(wl.getWorldSize() == 15);
        assertTrue(nonBlockables.size() == 1);
        assertTrue(animals.size() == 5);

        for(Animal animal : wl.getAnimals()) {
            if(!(animal instanceof Rabbit)) {
                fail("Incorrect Animal dynamic type, expected Rabbit");
            }
        }

        for(NonBlockable nonBlockable  : wl.getNonBlockables()) {
            if(!(nonBlockable instanceof OldRabbitHole)) {
                fail("Incorrect NonBlockable dynamic type, expected RabbitHole");
            }
        }
    }

    @Test
    public void t1_3b() {
        try{
            this.wl = new WorldLoader("resources/inputs/week-1/t1-3b.txt", 200, 200);
        }catch(Exception e) {
            fail("Input file t1-3b.txt not found");
        }

        List<NonBlockable> nonBlockables = wl.getNonBlockables();
        List<Animal> animals = wl.getAnimals();

        assertTrue(wl.getWorldSize() == 15);
        assertTrue(nonBlockables.size() == 1);
        assertTrue(animals.size() == 5);

        for(Animal animal : wl.getAnimals()) {
            if(!(animal instanceof Rabbit)) {
                fail("Incorrect Animal dynamic type, expected Rabbit");
            }
        }

        for(NonBlockable nonBlockable  : wl.getNonBlockables()) {
            if(!(nonBlockable instanceof OldRabbitHole)) {
                fail("Incorrect NonBlockable dynamic type, expected RabbitHole");
            }
        }
    }

    @Test
    public void tf1_1() {
        try{
            this.wl = new WorldLoader("resources/inputs/week-1/tf1-1.txt", 200, 200);
        }catch(Exception e) {
            fail("Input file tf1-1.txt not found");
        }

        List<NonBlockable> nonBlockables = wl.getNonBlockables();
        List<Animal> animals = wl.getAnimals();

        assertTrue(wl.getWorldSize() == 15);
        assertTrue(nonBlockables.size() == 3);
        assertTrue(animals.size() >= 3 && animals.size() <= 7);

        for(Animal animal : wl.getAnimals()) {
            if(!(animal instanceof Rabbit)) {
                fail("Incorrect Animal dynamic type, expected Rabbit");
            }
        }

        for(NonBlockable nonBlockable  : wl.getNonBlockables()) {
            if(!(nonBlockable instanceof OldRabbitHole)) {
                fail("Incorrect NonBlockable dynamic type, expected RabbitHole");
            }
        }
    }

}
