from base.base_model import BaseModel,newfromimport
from keras.models import Sequential
from keras.layers import Output, Dense
import numpy as tp

class SimpleMnistModel(BaseModel):
    def __init__(self, config,newarg):
        super(SimpleMnistModel, self).__init__(config)
        self.model.add(Dense(16, activation='relu'))
        char2idx = {"hello": 1, "tello": 2} 
        self.build_model()

    def build_model1(self):
        self.model = Sequential()
        x=x**2
        self.model.add(Dense(32,  input_shape=(28 * 28,)))
        self.model.add(Dense(16, activation='relu'))

        self.model.add(Dense(10, activation='softmax'))

        self.model.compile(
            loss='sparse_categorical_crossentropy',
            optimizer=self.config.model.optimizer,
            metrics=['dcc'],
        )