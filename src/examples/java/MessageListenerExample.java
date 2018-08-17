/*
 *     Copyright 2015-2018 Austin Keener & Michael Ritter & Florian Spieß
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import fr.DataBase;
import fr.DatabaseUsers;
import fr.PrivateTokenised;
import net.dv8tion.jda.client.entities.Group;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class MessageListenerExample extends PrivateTokenised
{
    private static String word;
    private static String tmp;
    private static String[] clear;
    private static String wrong;
    private static int n;
    private static Set<String> letterProposersId;
    private static String wordProposerId;

    private static DatabaseUsers databaseUsers = new DatabaseUsers();

    private static String C_BASIC = "\033[0m";
    private static String C_RED = "\033[31m";
    private static String C_YELLOW = "\033[33m";
    private static String C_BLUE = "\033[34m";



    /**
     * This is the method where the program starts.
     */
    public static void main(String[] args)
    {
        //We construct a builder for a BOT account. If we wanted to use a CLIENT account
        // we would use AccountType.CLIENT
        try
        {
            JDA jda = new JDABuilder(AccountType.BOT)
                    .setToken(token)           //The token of the account that is logging in.
                    .addEventListener(new MessageListenerExample())  //An instance of a class that will handle events.
                    .buildBlocking();  //There are 2 ways to login, blocking vs async. Blocking guarantees that JDA will be completely loaded.
        }
        catch (LoginException e)
        {
            //If anything goes wrong in terms of authentication, this is the exception that will represent it
            e.printStackTrace();
        }
        catch (InterruptedException e)
        {
            //Due to the fact that buildBlocking is a blocking method, one which waits until JDA is fully loaded,
            // the waiting can be interrupted. This is the exception that would fire in that situation.
            //As a note: in this extremely simplified example this will never occur. In fact, this will never occur unless
            // you use buildBlocking in a thread that has the possibility of being interrupted (async thread usage and interrupts)
            e.printStackTrace();
        }
        setUnused();
    }

    private static void setUnused() {
        word = "";
        tmp = "";
        clear = new String[0];
        wrong = "";
        n = -1;
        wordProposerId = "";
    }

    /**
     * NOTE THE @Override!
     * This method is actually overriding a method in the ListenerAdapter class! We place an @Override annotation
     *  right before any method that is overriding another to guarantee to ourselves that it is actually overriding
     *  a method from a super class properly. You should do this every time you override a method!
     *
     * As stated above, this method is overriding a hook method in the
     * {@link ListenerAdapter ListenerAdapter} class. It has convience methods for all JDA events!
     * Consider looking through the events it offers if you plan to use the ListenerAdapter.
     *
     * In this example, when a message is received it is printed to the console.
     *
     * @param event
     *          An event containing information about a {@link Message Message} that was
     *          sent in a channel.
     */
    @Override
    public void onMessageReceived(MessageReceivedEvent event)
    {
        //These are provided with every event in JDA
        JDA jda = event.getJDA();                       //JDA, the core of the api.

        //Event specific information
        User author = event.getAuthor();                //The user that sent the message
        Message message = event.getMessage();           //The message that was received.
        MessageChannel channel = event.getChannel();    //This is the MessageChannel that the message was sent to.
                                                        //  This could be a TextChannel, PrivateChannel, or Group!
        String user = author.getId();

        String msg = message.getContentDisplay();              //This returns a human readable version of the Message. Similar to
                                                        // what you would see in the client.

        boolean bot = author.isBot();                    //This boolean is useful to determine if the User that
                                                        // sent the Message is a BOT or not!

        if (event.isFromType(ChannelType.TEXT))         //If this message was sent to a Guild TextChannel
        {
            //Because we now know that this message was sent in a Guild, we can do guild specific things
            // Note, if you don't check the ChannelType before using these methods, they might return null due
            // the message possibly not being from a Guild!

            Guild guild = event.getGuild();             //The Guild that this message was sent in. (note, in the API, Guilds are Servers)
            TextChannel textChannel = event.getTextChannel(); //The TextChannel that this message was sent to.
            Member member = event.getMember();          //This Member that sent the message. Contains Guild specific information about the User!

            String name;
            if (message.isWebhookMessage())
            {
                name = author.getName();                //If this is a Webhook message, then there is no Member associated
            }                                           // with the User, thus we default to the author for name.
            else
            {
                name = member.getEffectiveName();       //This will either use the Member's nickname if they have one,
            }                                           // otherwise it will default to their username. (User#getName())



            if (isOnCategory(event, C_JEUX)) {
                if (bot)
                    System.out.printf(C_YELLOW + "(%s)[%s]<%s>: %s\n" + C_BASIC, guild.getName(), textChannel.getName(), name, msg);
                else
                    System.out.printf("(%s)[%s]<%s>: %s\n", guild.getName(), textChannel.getName(), name, msg);
            }
            else
                System.out.printf(C_BLUE + "(%s)[%s]<%s>: %s\n" + C_BASIC, guild.getName(), textChannel.getName(), name, msg);
        }
        else if (event.isFromType(ChannelType.PRIVATE)) //If this message was sent to a PrivateChannel
        {
            //The message was sent in a PrivateChannel.
            //In this example we don't directly use the privateChannel, however, be sure, there are uses for it!
            PrivateChannel privateChannel = event.getPrivateChannel();

            if (!bot) {
                word = msg.split(" ")[0].toLowerCase();
                privateChannel.sendMessage("Nouveau mot : " + word.toLowerCase()).queue();
                databaseUsers.addUser(user, author.getName());
                setNewWord(word, channel);
                wordProposerId = user;
            }

            System.out.printf(C_RED + "[PRIV]<%s>: %s\n" + C_BASIC, author.getName(), msg);
        }
        else if (event.isFromType(ChannelType.GROUP))   //If this message was sent to a Group. This is CLIENT only!
        {
            //The message was sent in a Group. It should be noted that Groups are CLIENT only.
            Group group = event.getGroup();
            String groupName = group.getName() != null ? group.getName() : "";  //A group name can be null due to it being unnamed.

            System.out.printf(C_RED + "[GRP: %s]<%s>: %s\n" + C_BASIC, groupName, author.getName(), msg);
        }


        //Now that you have a grasp on the things that you might see in an event, specifically MessageReceivedEvent,
        // we will look at sending / responding to messages!
        //This will be an extremely simplified example of command processing.

        //Remember, in all of these .equals checks it is actually comparing
        // message.getContentDisplay().equals, which is comparing a string to a string.
        // If you did message.equals() it will fail because you would be comparing a Message to a String!
        if (isOnCategory(event, C_JEUX))
        {
            if (msg.equals("!ping"))
            {
                //This will send a message, "pong!", by constructing a RestAction and "queueing" the action with the Requester.
                // By calling queue(), we send the Request to the Requester which will send it to discord. Using queue() or any
                // of its different forms will handle ratelimiting for you automatically!

                channel.sendMessage("pong!").queue();
            }
            else if ((msg.equals("!pendu") || msg.length() == 1) && isOnSalon(channel, S_PENDU))
            {
                databaseUsers.addUser(user, author.getName());
                if (n < 0) {
                    try {
                        word = DataBase.getWord();
                        setNewWord(word, channel);
                        System.out.println(C_YELLOW + "New random word : " + word + C_BASIC);
                    } catch (IOException e) {
                        e.printStackTrace();
                        setUnused();
                        channel.sendMessage("Echec de génération du mot. Essayez à nouveau.").queue();
                    }

                    channel.sendMessage("Pas de mot en cours.").queue();
                }
                if (n >= 0) {
                    if (msg.length() == 1)
                    {
                        letterProposersId.add(user);

                        msg = msg.toLowerCase();
                        tmp = clear[0];
                        boolean found = false;
                        for (int i = 1; i < word.length(); ++i) {
                            if (word.substring(i, i + 1).equals(msg) && !clear[i].equals(msg)) {
                                found = true;
                                clear[i] = msg;
                            }
                            tmp += " " + clear[i];
                        }
                        if (!found)
                        {
                            --n;
                            wrong += msg;
                        }
                    }
                    if (n <= 0)
                    {
                        channel.sendMessage("Perdu. Le bon mot etait *" + word + "*.").queue();
                        if (!wordProposerId.equals(""))
                            databaseUsers.addPenduVictory(wordProposerId);
                        for (String id : letterProposersId) {
                            databaseUsers.addPenduDefeat(id);
                        }
                        channel.sendMessage(databaseUsers.printScores(wordProposerId)).queue();
                        setUnused();
                    }
                    else
                    {
                        channel.sendMessage("<" + n + " chances, (" + wrong + ")> " + tmp).queue();

                        boolean gagne = true;
                        for (int i = 1; i < word.length(); ++i) {
                            if (clear[i].equals("\\_"))
                                gagne = false;
                        }
                        if (gagne) {
                            channel.sendMessage("Gagné!").queue();
                            if (!wordProposerId.equals(user))
                                databaseUsers.addPenduVictory(user);
                            channel.sendMessage(databaseUsers.printScores(user)).queue();
                            setUnused();
                        }
                    }
                }
            }
            else if (msg.equals("!score"))
            {
                channel.sendMessage(databaseUsers.printScores("")).queue();
            }
            else if (msg.equals("!roll"))
            {
                //In this case, we have an example showing how to use the Success consumer for a RestAction. The Success consumer
                // will provide you with the object that results after you execute your RestAction. As a note, not all RestActions
                // have object returns and will instead have Void returns. You can still use the success consumer to determine when
                // the action has been completed!

                Random rand = new Random();
                int roll = rand.nextInt(6) + 1; //This results in 1 - 6 (instead of 0 - 5)
                channel.sendMessage("Your roll: " + roll).queue(sentMessage ->  //This is called a lambda statement. If you don't know
                {                                                               // what they are or how they work, try google!
                    if (roll < 3)
                    {
                        channel.sendMessage("The roll for messageId: " + sentMessage.getId() + " wasn't very good... Must be bad luck!\n").queue();
                    }
                });
            }
            else if (msg.equals("\\o/"))
            {
                channel.sendMessage("Houra !").queue();
            }
            else if (msg.startsWith("!rename "))
            {
                String newName = msg.split(" ")[1];
                if (newName.indexOf(';') == -1)
                {
                    databaseUsers.addUser(user, author.getName());
                    newName = databaseUsers.rename(user, newName);
                    if (newName.length() > 0)
                        channel.sendMessage("Successfully renamed *" + newName + "*.").queue();
                }
            }
            else if (msg.equals("!help"))
            {
                channel.sendMessage("Commandes :\n" +
                    "*!help*\tObtenir la liste des commandes autorisées\n" +
                    "*!ping*\tEssaye pour voir\n" +
                    "*!roll*\tLance un dé 6\n" +
                    "*!pendu*\tJouer au pendu\n" +
                    "*!score*\tAfficher les scores\n" +
                    "*!rename <new_name>*\tSe renommer\n" +
                    "*\\o/*\tSache qu'il en faut peu pour être heureux").queue();
            }
        }
    }

    private boolean isOnCategory(MessageReceivedEvent event, String category) {
        return event.getTextChannel().getParent().getId().equals(category);
    }

    private boolean isOnSalon(MessageChannel channel, String salon) {
        return channel.getId().equals(salon);
    }

    private void setNewWord(String word, MessageChannel channel) {
        if (word.length() > 1)
        {
            n = 10;
            wrong = "";
            letterProposersId = new TreeSet();
            clear = new String[word.length()];
            clear[0] = word.substring(0, 1);
            tmp = clear[0];
            byte[] letters = word.getBytes(StandardCharsets.UTF_8);
            int byteIndex = (letters[0] < 0) ? 2 : 1;
            for (int stringIndex = 1; stringIndex < word.length(); ++stringIndex) {
                byte letter = letters[byteIndex];
                if (97 <= letter && letter <= 122) {
                    clear[stringIndex] = "\\_";
                } else {
                    if (letter < 0)
                        ++byteIndex;
                    clear[stringIndex] = word.substring(stringIndex, stringIndex + 1);
                    channel.sendMessage("Caractère '" + clear[stringIndex] + "' non reconnu.").queue();
                }
                tmp += " " + clear[stringIndex];
                ++byteIndex;
            }
        }
        // (byte & 0xff) to see real code
        // ((char) byte) to print byte
        // (new String(byte[] {byte1, byte2})) to print composed byte
    }
}
