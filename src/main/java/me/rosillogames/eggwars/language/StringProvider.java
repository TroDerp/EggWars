package me.rosillogames.eggwars.language;

import java.util.Random;

public abstract class StringProvider
{
    public abstract String getString();

    static class Default extends StringProvider
    {
        public final String translation;

        public Default(String t)
        {
            this.translation = t;
        }

        @Override
        public String getString()
        {
            return this.translation;
        }
    }

    static class Multiple extends StringProvider
    {
        public final String[] translations;

        Multiple(String[] t)
        {
            this.translations = t;
        }

        @Override
        public String getString()
        {
            return this.translations[(new Random()).nextInt(this.translations.length)];
        }
    }

    static class Reference extends StringProvider
    {
        private static int attempts = 0;
        public Language lang;
        public final String reference;

        Reference(String refIn)
        {
            this.reference = refIn;
        }

        @Override
        public String getString()
        {
            if (attempts > 4)
            {
                attempts = 0;
                return this.reference;
            }

            StringProvider refProvider = this.lang.getOrDefault(this.reference);

            if (refProvider instanceof Reference)
            {
                attempts++;
            }
            else
            {
                attempts = 0;
            }

            return refProvider.getString();
        }

        public void setLang(Language langIn)
        {
            this.lang = langIn;
        }
    }
}
