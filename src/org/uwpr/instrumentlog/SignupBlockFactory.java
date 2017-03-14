package org.uwpr.instrumentlog;

/**
 * Created by vsharma on 2/10/2017.
 */
public interface SignupBlockFactory <T extends SignupBlock>
{
    T create();

    class SignupBlockCreator implements SignupBlockFactory<SignupBlock>
    {
        @Override
        public SignupBlock create()
        {
            return new SignupBlock();
        }
    }

    class SignupBlockWithRateCreator implements SignupBlockFactory<SignupBlockWithRate>
    {
        @Override
        public SignupBlockWithRate create()
        {
            return new SignupBlockWithRate();
        }
    }
}


